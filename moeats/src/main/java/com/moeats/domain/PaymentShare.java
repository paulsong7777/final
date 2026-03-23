package com.moeats.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class PaymentShare {
	int paymentShareIdx;
	int paymentIdx;
	int memberIdx;
	int shareAmount;
	String payMethod;
	String shareStatus;
	Timestamp paidAt;
	Timestamp createdAt;
	Timestamp updatedAt;
}
