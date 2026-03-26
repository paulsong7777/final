package com.moeats.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class GroupCartItem {
	int cartItemIdx;
	int roomIdx;
	int memberIdx;
	int menuIdx;
	int itemQuantity;
	int baseAmount;
	int optionExtraAmount;
	int itemTotalAmount;
	String itemStatus;
	Timestamp createdAt;
	Timestamp updatedAt;
}
