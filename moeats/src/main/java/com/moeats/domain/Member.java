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
public class Member {
	// 회원정보
	int member_idx;							// 회원번호
	String member_email;					// 회원이메일
	String member_password;					// 비밀번호
	String member_nickname;					// 닉네임
	String member_phone;					// 전화번호
	String member_role_type;				// 회원타입(일반,가게)
	String default_delivery_address_idx;	// 기본 배송지
	Timestamp created_at;						// 가입날짜
	Timestamp updated_at;						// 수정날짜
}
