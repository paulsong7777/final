package com.moeats.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class GroupOrderItem {
	int orderItemIdx;
	int orderIdx;
	int memberIdx;
	int menuIdx;
	String menuNameSnapshot;
	int menuPriceSnapshot;
	int itemQuantity;
	int baseAmount;
	int optionExtraAmount;
	int itemTotalAmount;
	Timestamp createdAt;
	Timestamp updatedAt;
}
