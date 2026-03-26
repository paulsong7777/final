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
	
	public void setFrom(int orderIdx,DeliveryAddress deliveryAddress) {
		setOrderIdx(orderIdx);
		setSourceDeliveryAddressIdx(deliveryAddress.getDeliveryAddressIdx());
		setRecipientName(deliveryAddress.getRecipientName());
		setRecipientPhone(deliveryAddress.getRecipientPhone());
		setZipCode(deliveryAddress.getZipCode());
		setDeliveryAddress1(deliveryAddress.getDeliveryAddress1());
		setDeliveryAddress2(deliveryAddress.getDeliveryAddress2());
		setDeliveryRequest(deliveryAddress.getDeliveryRequest());
	}
}
