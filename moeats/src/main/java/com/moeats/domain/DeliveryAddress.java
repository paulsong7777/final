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
public class DeliveryAddress {
	// 배송지 주소 정보
	int deliveryAddressIdx;			// 배송지번호
	int memberIdx;					// 회원번호
	String deliveryLabel; 			// 배송지별칭 집/회사 등
	String recipientName; 			// 수령인명
	String recipientPhone;			// 연락처
	String zipCode;					// 우편번호
	String deliveryAddress1;		// 도로명/지번주소
	String deliveryAddress2;		// 상세주소
	String deliveryRequest;		// 배송요청사항
	boolean isActive;				// 사용여부
	Timestamp createdAt;			// 생성시각
	Timestamp updatedAt;			// 수정시각
	
	// 사용자의 주소 좌표
	Double longitude;				// 경도
	Double latitude;					// 위도
	
	// 주소 좌표 가져오기 위해서 임시 추가
	private String jibunAddress;
}
