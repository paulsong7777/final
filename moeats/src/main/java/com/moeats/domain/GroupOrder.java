package com.moeats.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class GroupOrder {
	int orderIdx;
	int roomIdx;
	int storeIdx;
	int leaderMemberIdx;
	String orderMode;
	String paymentMode;
	String orderStatus;
	int orderTotalAmount;
	Timestamp expectedVisitAt;
	Timestamp paymentConfirmedAt;
	Timestamp storeConfirmedAt;
	Timestamp checkedInAt;
	Timestamp completedAt;
	Timestamp cancelledAt;
	Timestamp createdAt;
	Timestamp updatedAt;

	
	public static GroupOrder from(OrderRoom orderRoom) {
		GroupOrder groupOrder = new GroupOrder(); 
		groupOrder.setRoomIdx(orderRoom.getRoomIdx());
		groupOrder.setStoreIdx(orderRoom.getStoreIdx());
		groupOrder.setLeaderMemberIdx(orderRoom.getLeaderMemberIdx());
		groupOrder.setOrderMode(orderRoom.getOrderMode());
		groupOrder.setPaymentMode(orderRoom.getPaymentMode());
		return groupOrder;
	}

}
