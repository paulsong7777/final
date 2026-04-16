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
	
	// 🌟 이 필드를 추가해야 서비스에서 별명을 담아줄 수 있습니다!
    private String memberNickname;
}
