package com.moeats.domain;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuImage {
	int menuImageIdx;		// 메뉴 이미지 번호
	int menuIdx;			// 메뉴 번호
	String imageUrl;		// 이미지 경로
	boolean	isPrimary; 		// 대표 이미지 여부
	int displayOrder;		// 노출 순서 정렬순서
	Timestamp createdAt;	// 생성일시

}
