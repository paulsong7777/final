package com.moeats.domain;

<<<<<<< HEAD

=======
>>>>>>> a241daed0341297332c508edf883a682b80f8ba0
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
	int memberIdx;						// 회원번호
	String memberEmail;					// 회원이메일
	String memberPassword;				// 비밀번호
	String memberNickname;				// 닉네임
	String memberPhone;					// 전화번호
	String memberRoleType;				// 회원타입(일반,가게)
	String defaultDeliveryAddressIdx;	// 기본 배송지
	String memberStatus;				// 회원상태
	Timestamp createdAt;				// 가입날짜
	Timestamp updatedAt;				// 수정날짜
}
