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
<<<<<<< HEAD
=======
	
	public static OrderDelivery from(int orderIdx,DeliveryAddress deliveryAddress) {
		OrderDelivery orderDelivery = new OrderDelivery();
		orderDelivery.setOrderIdx(orderIdx);
		orderDelivery.setSourceDeliveryAddressIdx(deliveryAddress.getDeliveryAddressIdx());
		orderDelivery.setRecipientName(deliveryAddress.getRecipientName());
		orderDelivery.setRecipientPhone(deliveryAddress.getRecipientPhone());
		orderDelivery.setZipCode(deliveryAddress.getZipCode());
		orderDelivery.setDeliveryAddress1(deliveryAddress.getDeliveryAddress1());
		orderDelivery.setDeliveryAddress2(deliveryAddress.getDeliveryAddress2());
		orderDelivery.setDeliveryRequest(deliveryAddress.getDeliveryRequest());
		return orderDelivery;
	}
>>>>>>> origin/integration
}
