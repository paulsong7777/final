package com.moeats.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class MenuImage {
	int menuImageIdx;
	int menuIdx;
	String imageUrl;
	boolean isPrimary;
	int displayOrder;
	Timestamp createdAt;
}
