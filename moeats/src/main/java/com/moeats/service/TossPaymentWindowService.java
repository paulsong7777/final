package com.moeats.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeats.config.TossPaymentsProperties;
import com.moeats.domain.PaymentTransaction;
import com.moeats.domain.TossCheckoutTarget;
import com.moeats.domain.TossCreatePaymentRequest;
import com.moeats.domain.TossCreatePaymentResponse;
import com.moeats.mapper.PaymentTransactionMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class TossPaymentWindowService {

    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PaymentTransactionMapper paymentTransactionMapper;
    private final TossPaymentsProperties tossPaymentsProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public TossPaymentWindowService(PaymentTransactionMapper paymentTransactionMapper,
                                    TossPaymentsProperties tossPaymentsProperties,
                                    ObjectMapper objectMapper) {
        this.paymentTransactionMapper = paymentTransactionMapper;
        this.tossPaymentsProperties = tossPaymentsProperties;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(tossPaymentsProperties.getApiBaseUrl())
                .build();
    }

    @Transactional
    public String openRepresentativeCheckout(Long paymentIdx, String baseUrl) {
        TossCheckoutTarget target = paymentTransactionMapper.selectRepresentativeTarget(paymentIdx);

        if (target == null) {
            throw new IllegalArgumentException("대표결제 대상 결제건을 찾지 못했습니다.");
        }
        if (!"REPRESENTATIVE".equals(target.getPaymentMode())) {
            throw new IllegalArgumentException("대표결제 전용 결제건이 아닙니다.");
        }
        if ("PAID".equals(target.getPaymentStatus())) {
            throw new IllegalStateException("이미 결제가 완료된 주문입니다.");
        }

        int attemptNo = safeAttempt(paymentTransactionMapper.selectNextRepresentativeAttemptNo(paymentIdx));
        PaymentTransaction transaction = buildRepresentativeTransaction(target, attemptNo);
        paymentTransactionMapper.insertPaymentTransaction(transaction);

        TossCreatePaymentRequest request = new TossCreatePaymentRequest();
        request.setMethod("CARD");
        request.setAmount(target.getAmount());
        request.setCurrency("KRW");
        request.setOrderId(transaction.getMerchantOrderId());
        request.setOrderName(target.getOrderName());
        request.setSuccessUrl(resolveSuccessUrl(baseUrl));
        request.setFailUrl(resolveFailUrl(baseUrl));

        return createPaymentWindow(transaction.getPaymentTransactionIdx(), transaction.getIdempotencyKey(), request);
    }

    @Transactional
    public String openIndividualCheckout(Long paymentShareIdx, String baseUrl) {
        TossCheckoutTarget target = paymentTransactionMapper.selectIndividualTarget(paymentShareIdx);

        if (target == null) {
            throw new IllegalArgumentException("각자결제 대상 분담건을 찾지 못했습니다.");
        }
        if (!"INDIVIDUAL".equals(target.getPaymentMode())) {
            throw new IllegalArgumentException("각자결제 전용 분담건이 아닙니다.");
        }
        if (!"PENDING".equals(target.getShareStatus())) {
            throw new IllegalStateException("이미 처리된 결제분담입니다.");
        }
        if (target.getPaymentExpiresAt() != null && target.getPaymentExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("결제 가능 시간이 이미 만료되었습니다.");
        }
        if ("PAID".equals(target.getPaymentStatus()) || "CANCELLED".equals(target.getPaymentStatus())) {
            throw new IllegalStateException("진행 가능한 결제 상태가 아닙니다.");
        }

        int attemptNo = safeAttempt(paymentTransactionMapper.selectNextIndividualAttemptNo(paymentShareIdx));
        PaymentTransaction transaction = buildIndividualTransaction(target, attemptNo);
        paymentTransactionMapper.insertPaymentTransaction(transaction);

        TossCreatePaymentRequest request = new TossCreatePaymentRequest();
        request.setMethod("CARD");
        request.setAmount(target.getAmount());
        request.setCurrency("KRW");
        request.setOrderId(transaction.getMerchantOrderId());
        request.setOrderName(target.getOrderName());
        request.setSuccessUrl(resolveSuccessUrl(baseUrl));
        request.setFailUrl(resolveFailUrl(baseUrl));

        return createPaymentWindow(transaction.getPaymentTransactionIdx(), transaction.getIdempotencyKey(), request);
    }

    private PaymentTransaction buildRepresentativeTransaction(TossCheckoutTarget target, int attemptNo) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentIdx(target.getPaymentIdx());
        transaction.setPaymentShareIdx(null);
        transaction.setMemberIdx(target.getMemberIdx());
        transaction.setAttemptNo(attemptNo);
        transaction.setProvider("TOSS");
        transaction.setTransactionType("REPRESENTATIVE");
        transaction.setMerchantOrderId("MOEATS-R-" + target.getPaymentIdx() + "-" + LocalDateTime.now().format(ORDER_TIME_FORMATTER));
        transaction.setCurrency("KRW");
        transaction.setRequestAmount(target.getAmount());
        transaction.setCancelledAmount(0);
        transaction.setTransactionStatus("READY");
        transaction.setIdempotencyKey("CREATE-" + transaction.getMerchantOrderId());
        return transaction;
    }

    private PaymentTransaction buildIndividualTransaction(TossCheckoutTarget target, int attemptNo) {
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentIdx(target.getPaymentIdx());
        transaction.setPaymentShareIdx(target.getPaymentShareIdx());
        transaction.setMemberIdx(target.getMemberIdx());
        transaction.setAttemptNo(attemptNo);
        transaction.setProvider("TOSS");
        transaction.setTransactionType("INDIVIDUAL");
        transaction.setMerchantOrderId("MOEATS-I-" + target.getPaymentShareIdx() + "-" + attemptNo + "-"
                + LocalDateTime.now().format(ORDER_TIME_FORMATTER));
        transaction.setCurrency("KRW");
        transaction.setRequestAmount(target.getAmount());
        transaction.setCancelledAmount(0);
        transaction.setTransactionStatus("READY");
        transaction.setIdempotencyKey("CREATE-" + transaction.getMerchantOrderId());
        return transaction;
    }

    private String createPaymentWindow(Long paymentTransactionIdx, String idempotencyKey, TossCreatePaymentRequest request) {
        try {
            TossCreatePaymentResponse response = restClient.post()
                    .uri("/v1/payments")
                    .header(HttpHeaders.AUTHORIZATION, createBasicAuthHeader())
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(TossCreatePaymentResponse.class);

            if (response == null || response.getCheckout() == null || response.getCheckout().getUrl() == null) {
                paymentTransactionMapper.markTransactionFailed(paymentTransactionIdx, "CHECKOUT_URL_EMPTY",
                        "checkout.url 값이 비어 있습니다.", "{}");
                throw new IllegalStateException("결제창 URL을 받지 못했습니다.");
            }

            return response.getCheckout().getUrl();
        } catch (Exception e) {
            paymentTransactionMapper.markTransactionFailed(paymentTransactionIdx,
                    "CREATE_PAYMENT_FAILED", e.getMessage(), safeToJson(request));
            throw new IllegalStateException("결제창 생성 중 오류가 발생했습니다. " + e.getMessage(), e);
        }
    }

    private int safeAttempt(Integer attemptNo) {
        return attemptNo == null || attemptNo < 1 ? 1 : attemptNo;
    }

    private String createBasicAuthHeader() {
        String auth = tossPaymentsProperties.getSecretKey() + ":";
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private String safeToJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
    private String resolveSuccessUrl(String baseUrl) {
        if (baseUrl != null && !baseUrl.isBlank()) {
            return trimTrailingSlash(baseUrl) + "/sandbox/toss/success";
        }
        return tossPaymentsProperties.getSuccessUrl();
    }

    private String resolveFailUrl(String baseUrl) {
        if (baseUrl != null && !baseUrl.isBlank()) {
            return trimTrailingSlash(baseUrl) + "/sandbox/toss/fail";
        }
        return tossPaymentsProperties.getFailUrl();
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
    
}
