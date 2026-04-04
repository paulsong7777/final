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
public class Store {
	// 가게 정보
	int storeIdx;						// 가게번호
	int ownerMemberIdx;			// 점주 회원번호
	String storeName;				// 매장명
	String storeCategory;			// 가게 카테고리
	String storeDescription;		// 가게 소개
	String storePhone;				// 가게 전화
	int minimumOrderAmount;	// 배달 최소 주문 금액
	String storeAddress1;			// 기본 주소
	String storeAddress2;			// 상세 주소
	Boolean supportsDelivery;	// 배달지원여부
	Boolean supportsOnsite;		// 현장지원여부
	String storeStatus;				// 가게 상태(영업중, 영업 중단, 폐업 등)
	Timestamp createdAt;			// 생성 시각
	Timestamp updatedAt;			// 수정 시각
	Double longitude;				// 경도
	Double latitude;					//	위도
	
	int deliveryRadius;				// 배달 가능 반경
}
