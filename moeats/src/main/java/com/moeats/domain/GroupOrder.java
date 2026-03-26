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
	
	public void setFrom(OrderRoom orderRoom) {
		setRoomIdx(orderRoom.getRoomIdx());
		setStoreIdx(orderRoom.getStoreIdx());
		setLeaderMemberIdx(orderRoom.getLeaderMemberIdx());
		setOrderMode(orderRoom.getOrderMode());
		setPaymentMode(orderRoom.getPaymentMode());
	}
}
