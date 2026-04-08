package com.moeats.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.Member;

@Mapper
public interface MemberMapper {
	
	// 기본 배송지 설정 
    public void updateDefaultAddressIdx(@Param("memberIdx") int memberIdx,
            @Param("deliveryAddressIdx") int deliveryAddressIdx);
    
    // 기본 배송지 해제
    public void clearDefaultAddressIdx(@Param("memberIdx") int memberIdx);
	
	// 기본 배송지 가져옴
	public Integer getDefaultAddressIdx(int memberIdx);
	
	// 회원정보 수정
	public void updateMember(Member member);
	
	// 비밀번호 확인
	public String isPassCheck(int memberIdx);
	
	// 회원가입
	public void insertMember(Member member);
	
	// 회원 이메일 조회
	public Member getMemberFromEmail(String memberEmail);
	
	// 회원 조회
	public Member getMember(int memberIdx);
	
}
