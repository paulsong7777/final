package com.moeats.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class Member {
	int memberIdx;
	String memberEmail;
	String memberPassword;
	String memberNickname;
	String memberPhone;
	String memberRoleType;
	int defaultDeliveryAddressIdx;
	String memberStatus;
	Timestamp createdAt;
	Timestamp updatedAt;
}
