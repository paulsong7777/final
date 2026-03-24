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
	int store_idx;				// 가게번호
	int owner_member_idx;		// 점주 회원번호
	String store_name;			// 매장명
	String store_description;	// 가게 소개
	String store_phone;			// 가게 전화
	int minimum_order_amount;	// 배달 최소 주문 금액
	String store_address1;		// 기본 주소
	String store_address2;		// 상세 주소
	boolean supports_delivert;	// 배달지원여부
	boolean supports_onsite;	// 현장지원여부
	Timestamp created_at;			// 생성 시각
	Timestamp updated_at;			// 수정 시각
}
