package com.moeats.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class RoomParticipant {
	int roomParticipantIdx;
	int roomIdx;
	int memberIdx;
	String participantRole;
	String selectionStatus;
	String paymentStatus;
	Timestamp joinedAt;
	Timestamp leftAt;
	Timestamp createdAt;
	Timestamp updatedAt;
}
