package com.moeats.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class StoreMenu {
	int menuIdx;
	int storeIdx;
	String menuName;
	String menuDescription;
	int menuPrice;
	String menuStatus;
	int displayOrder;
	Timestamp createdAt;
	Timestamp updatedAt;
}
