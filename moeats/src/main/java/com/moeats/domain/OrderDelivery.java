package com.moeats.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class OrderDelivery {
	int orderDeliveryIdx;
	int orderIdx;
	int sourceDeliveryAddressIdx;
	String recipientName;
	String recipientPhone;
	String zipCode;
	String deliveryAddress1;
	String deliveryAddress2;
	String deliveryRequest;
	Timestamp createdAt;
	Timestamp updatedAt;
}
