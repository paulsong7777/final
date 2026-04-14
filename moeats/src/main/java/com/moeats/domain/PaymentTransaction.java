package com.moeats.domain;

import java.time.LocalDateTime;

public class PaymentTransaction {

    private Long paymentTransactionIdx;
    private Long paymentIdx;
    private Long paymentShareIdx;
    private Long memberIdx;
    private Integer attemptNo;

    private String provider;
    private String transactionType;
    private String merchantOrderId;
    private String providerPaymentKey;
    private String providerMethod;
    private String currency;

    private Integer requestAmount;
    private Integer approvedAmount;
    private Integer cancelledAmount;

    private String transactionStatus;
    private String idempotencyKey;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime expiredAt;
    private LocalDateTime failedAt;

    private String failCode;
    private String failMessage;
    private String rawResponse;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getPaymentTransactionIdx() {
        return paymentTransactionIdx;
    }

    public void setPaymentTransactionIdx(Long paymentTransactionIdx) {
        this.paymentTransactionIdx = paymentTransactionIdx;
    }

    public Long getPaymentIdx() {
        return paymentIdx;
    }

    public void setPaymentIdx(Long paymentIdx) {
        this.paymentIdx = paymentIdx;
    }

    public Long getPaymentShareIdx() {
        return paymentShareIdx;
    }

    public void setPaymentShareIdx(Long paymentShareIdx) {
        this.paymentShareIdx = paymentShareIdx;
    }

    public Long getMemberIdx() {
        return memberIdx;
    }

    public void setMemberIdx(Long memberIdx) {
        this.memberIdx = memberIdx;
    }

    public Integer getAttemptNo() {
        return attemptNo;
    }

    public void setAttemptNo(Integer attemptNo) {
        this.attemptNo = attemptNo;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getMerchantOrderId() {
        return merchantOrderId;
    }

    public void setMerchantOrderId(String merchantOrderId) {
        this.merchantOrderId = merchantOrderId;
    }

    public String getProviderPaymentKey() {
        return providerPaymentKey;
    }

    public void setProviderPaymentKey(String providerPaymentKey) {
        this.providerPaymentKey = providerPaymentKey;
    }

    public String getProviderMethod() {
        return providerMethod;
    }

    public void setProviderMethod(String providerMethod) {
        this.providerMethod = providerMethod;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getRequestAmount() {
        return requestAmount;
    }

    public void setRequestAmount(Integer requestAmount) {
        this.requestAmount = requestAmount;
    }

    public Integer getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(Integer approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    public Integer getCancelledAmount() {
        return cancelledAmount;
    }

    public void setCancelledAmount(Integer cancelledAmount) {
        this.cancelledAmount = cancelledAmount;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public LocalDateTime getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(LocalDateTime expiredAt) {
        this.expiredAt = expiredAt;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }

    public String getFailCode() {
        return failCode;
    }

    public void setFailCode(String failCode) {
        this.failCode = failCode;
    }

    public String getFailMessage() {
        return failMessage;
    }

    public void setFailMessage(String failMessage) {
        this.failMessage = failMessage;
    }

    public String getRawResponse() {
        return rawResponse;
    }

    public void setRawResponse(String rawResponse) {
        this.rawResponse = rawResponse;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
