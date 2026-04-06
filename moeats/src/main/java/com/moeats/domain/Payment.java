package com.moeats.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class Payment {
	int paymentIdx;
	int orderIdx;
	String paymentMode;
	int paymentRequestAmount;
	int paymentPaidAmount;
	String paymentStatus;
	Timestamp paymentStartedAt;
	Timestamp paymentExpiresAt;
	Timestamp paidAt;
	Timestamp cancelledAt;
	Timestamp createdAt;
	Timestamp updatedAt;
<<<<<<< HEAD
=======
	
	public static Payment from(GroupOrder groupOrder) {
		Payment payment = new Payment();
		payment.setOrderIdx(groupOrder.getOrderIdx());
		payment.setPaymentMode(groupOrder.getPaymentMode());
		payment.setPaymentRequestAmount(groupOrder.getOrderTotalAmount());
		return payment;
	}
>>>>>>> origin/integration
}
