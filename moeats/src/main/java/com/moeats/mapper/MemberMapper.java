package com.moeats.mapper;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.Member;

@Mapper
public interface MemberMapper {
	
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
	
	public List<Member> getMembers(@Param("memberIdxes") Set<Integer> memberIdxes);
}
