package com.moeats.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moeats.config.TossPaymentsProperties;
import com.moeats.domain.PaymentTransaction;
import com.moeats.domain.TossCancelRequest;
import com.moeats.domain.TossConfirmRequest;
import com.moeats.domain.TossPaymentResponse;
import com.moeats.mapper.PaymentTransactionMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class TossPaymentCallbackService {

    private final PaymentTransactionMapper paymentTransactionMapper;
    private final TossPaymentsProperties tossPaymentsProperties;
    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public TossPaymentCallbackService(PaymentTransactionMapper paymentTransactionMapper,
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
    public TossPaymentResponse confirm(String paymentKey, String orderId, Integer amount) {
        PaymentTransaction transaction = paymentTransactionMapper.selectByMerchantOrderId(orderId);

        if (transaction == null) {
            throw new IllegalArgumentException("결제 거래를 찾지 못했습니다. orderId=" + orderId);
        }
        if ("DONE".equals(transaction.getTransactionStatus())) {
            TossPaymentResponse alreadyDone = new TossPaymentResponse();
            alreadyDone.setPaymentKey(transaction.getProviderPaymentKey());
            alreadyDone.setOrderId(transaction.getMerchantOrderId());
            alreadyDone.setMethod(transaction.getProviderMethod());
            alreadyDone.setStatus("DONE");
            alreadyDone.setTotalAmount(transaction.getApprovedAmount());
            return alreadyDone;
        }
        if (!transaction.getRequestAmount().equals(amount)) {
            paymentTransactionMapper.markTransactionFailed(transaction.getPaymentTransactionIdx(),
                    "AMOUNT_MISMATCH", "성공 콜백 금액과 서버 저장 금액이 다릅니다.", "{}");
            throw new IllegalStateException("결제 금액이 일치하지 않습니다.");
        }

        paymentTransactionMapper.markTransactionInProgress(transaction.getPaymentTransactionIdx());

        TossConfirmRequest request = new TossConfirmRequest(paymentKey, orderId, amount);

        try {
            TossPaymentResponse response = restClient.post()
                    .uri("/v1/payments/confirm")
                    .header(HttpHeaders.AUTHORIZATION, createBasicAuthHeader())
                    .header("Idempotency-Key", "CONFIRM-" + transaction.getPaymentTransactionIdx() + "-" + transaction.getAttemptNo())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(TossPaymentResponse.class);

            if (response == null) {
                throw new IllegalStateException("승인 응답이 비어 있습니다.");
            }

            paymentTransactionMapper.markTransactionDone(
                    transaction.getPaymentTransactionIdx(),
                    response.getPaymentKey(),
                    response.getMethod(),
                    response.getTotalAmount(),
                    safeToJson(response)
            );

            if ("REPRESENTATIVE".equals(transaction.getTransactionType())) {
                paymentTransactionMapper.markAllSharesPaidByRepresentative(transaction.getPaymentIdx());
                paymentTransactionMapper.markRoomParticipantsPaidByPayment(transaction.getPaymentIdx());
                paymentTransactionMapper.updatePaymentProgress(transaction.getPaymentIdx(), "PAID", response.getTotalAmount());
                paymentTransactionMapper.updateGroupOrderPaidByPayment(transaction.getPaymentIdx());
                paymentTransactionMapper.updateOrderRoomConfirmedByPayment(transaction.getPaymentIdx());
            } else {
                paymentTransactionMapper.markSharePaidSelf(transaction.getPaymentShareIdx());
                paymentTransactionMapper.markRoomParticipantPaidByShare(transaction.getPaymentShareIdx());

                Integer paidAmount = zeroIfNull(paymentTransactionMapper.selectPaidAmount(transaction.getPaymentIdx()));
                Integer pendingCount = zeroIfNull(paymentTransactionMapper.countPendingShares(transaction.getPaymentIdx()));

                if (pendingCount == 0) {
                    paymentTransactionMapper.updatePaymentProgress(transaction.getPaymentIdx(), "PAID", paidAmount);
                    paymentTransactionMapper.updateGroupOrderPaidByPayment(transaction.getPaymentIdx());
                    paymentTransactionMapper.updateOrderRoomConfirmedByPayment(transaction.getPaymentIdx());
                } else {
                    paymentTransactionMapper.updatePaymentProgress(transaction.getPaymentIdx(), "IN_PROGRESS", paidAmount);
                }
            }

            return response;
        } catch (Exception e) {
            paymentTransactionMapper.markTransactionFailed(transaction.getPaymentTransactionIdx(),
                    "CONFIRM_FAILED", e.getMessage(), safeToJson(request));
            throw new IllegalStateException("결제 승인 중 오류가 발생했습니다. " + e.getMessage(), e);
        }
    }

    @Transactional
    public void handleFail(String orderId, String code, String message) {
        if (orderId == null || orderId.isBlank()) {
            return;
        }

        PaymentTransaction transaction = paymentTransactionMapper.selectByMerchantOrderId(orderId);
        if (transaction == null) {
            return;
        }
        if ("DONE".equals(transaction.getTransactionStatus())) {
            return;
        }

        paymentTransactionMapper.markTransactionFailed(transaction.getPaymentTransactionIdx(),
                defaultString(code, "PAYMENT_FAILED"),
                defaultString(message, "결제에 실패했습니다."),
                "{}");
    }

    @Transactional
    public void cancelOneDoneTransaction(PaymentTransaction transaction, String reason) {
        if (transaction.getProviderPaymentKey() == null || transaction.getProviderPaymentKey().isBlank()) {
            paymentTransactionMapper.markTransactionCancelPending(
                    transaction.getPaymentTransactionIdx(),
                    "paymentKey가 없어 취소 API를 호출할 수 없습니다."
            );
            return;
        }

        TossCancelRequest request = new TossCancelRequest(reason);

        try {
            TossPaymentResponse response = restClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", transaction.getProviderPaymentKey())
                    .header(HttpHeaders.AUTHORIZATION, createBasicAuthHeader())
                    .header("Idempotency-Key", "CANCEL-" + transaction.getPaymentTransactionIdx())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(TossPaymentResponse.class);

            paymentTransactionMapper.markTransactionCancelled(
                    transaction.getPaymentTransactionIdx(),
                    transaction.getApprovedAmount(),
                    safeToJson(response)
            );
        } catch (Exception e) {
            paymentTransactionMapper.markTransactionCancelPending(
                    transaction.getPaymentTransactionIdx(),
                    e.getMessage()
            );
        }
    }

    private String createBasicAuthHeader() {
        String auth = tossPaymentsProperties.getSecretKey() + ":";
        String encoded = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private Integer zeroIfNull(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultString(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String safeToJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }
}
