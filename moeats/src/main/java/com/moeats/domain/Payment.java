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
}
