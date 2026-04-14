package com.moeats.domain;

import java.time.LocalDateTime;

public class TossCheckoutTarget {

    private Long paymentIdx;
    private Long paymentShareIdx;
    private Long orderIdx;
    private Long roomIdx;
    private Long memberIdx;

    private String paymentMode;
    private String paymentStatus;
    private String shareStatus;

    private Integer amount;
    private String orderName;
    private LocalDateTime paymentExpiresAt;

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

    public Long getOrderIdx() {
        return orderIdx;
    }

    public void setOrderIdx(Long orderIdx) {
        this.orderIdx = orderIdx;
    }

    public Long getRoomIdx() {
        return roomIdx;
    }

    public void setRoomIdx(Long roomIdx) {
        this.roomIdx = roomIdx;
    }

    public Long getMemberIdx() {
        return memberIdx;
    }

    public void setMemberIdx(Long memberIdx) {
        this.memberIdx = memberIdx;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getShareStatus() {
        return shareStatus;
    }

    public void setShareStatus(String shareStatus) {
        this.shareStatus = shareStatus;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public LocalDateTime getPaymentExpiresAt() {
        return paymentExpiresAt;
    }

    public void setPaymentExpiresAt(LocalDateTime paymentExpiresAt) {
        this.paymentExpiresAt = paymentExpiresAt;
    }
}
