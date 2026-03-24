package com.moeats.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.moeats.domain.Member;
import com.moeats.mapper.MemberMapper;

@Service
public class MemberService {
	
	@Autowired
	private MemberMapper memberMapper;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	// 회원정보 수정
	public void updateMember(Member member) {
		
		if(member.getMember_password() != null && !member.getMember_password().isEmpty()) {
			String encoded = passwordEncoder.encode(member.getMember_password());
			member.setMember_password(encoded);
		}
		
		memberMapper.updateMember(member);
	}
	
	// 비밀번호 확인
	public boolean isPassCheck(int member_idx, String member_password) {
		boolean result = false;
		
		String dbpass = memberMapper.isPassCheck(member_idx);
		
		return passwordEncoder.matches(member_password, dbpass);
		
	}
	
	// 로그인
	public Member login(String member_email, String member_password) {

		Member member = memberMapper.getMemberFromEmail(member_email);
		
		if(member == null) {
			return null;
		}
		
		if(passwordEncoder.matches(member_password, member.getMember_password())) {
			return member;
		}
		return null;
	};
	
	
	// 회원 가입
	public void insertMember(Member member) {
			
		// 비밀번호 암호화 기능
		String encoded = passwordEncoder.encode(member.getMember_password());
		member.setMember_password(encoded);
		
		memberMapper.insertMember(member);
	}
	
	// 회원 이메일 조회
	public Member getMemberFromEmail(String member_email) {
		
		return memberMapper.getMemberFromEmail(member_email);
	}
	
	// 회원 정보 조회
	public Member getMember(int member_idx) {
		return memberMapper.getMember(member_idx);
	}
}
