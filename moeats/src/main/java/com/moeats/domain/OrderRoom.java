package com.moeats.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class OrderRoom {
	int roomIdx;
	int leaderMemberIdx;
	int storeIdx;
	int	selectedDeliveryAddressIdx;
	String roomCode;
	String orderMode;
	String paymentMode;
	String roomStatus;
	boolean isJoinLocked;
	Timestamp lockedAt;
	Timestamp expiresAt;
	Timestamp createdAt;
	Timestamp updatedAt;
}
