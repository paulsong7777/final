package com.moeats.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class Store {
	int storeIdx;
	int ownerMemberIdx;
	String storeName;
	String storeDescription;
	String storePhone;
	int minimumOrderAmount;
	String storeAddress1;
	String storeAddress2;
	boolean supportsDelivery;
	boolean supportsOnsite;
	String storeStatus;
	double longitude;
	double latitude;
	Timestamp createdAt;
	Timestamp updatedAt;
}
