package com.moeats.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class DeliveryAddress {
	int deliveryAddressIdx;
	int memberIdx;
	String deliveryLabel;
	String recipientName;
	String recipientPhone;
	String zipCode;
	String deliveryAddress1;
	String deliveryAddress2;
	String deliveryRequest;
	boolean isActive;
	Timestamp createdAt;
	Timestamp updatedAt;
}
