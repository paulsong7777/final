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
		
		if(member.getMemberPassword() != null && !member.getMemberPassword().isEmpty()) {
			String encoded = passwordEncoder.encode(member.getMemberPassword());
			member.setMemberPassword(encoded);
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
		
		if(passwordEncoder.matches(member_password, member.getMemberPassword())) {
			return member;
		}
		return null;
	};
	
	
	// 회원 가입
	public void insertMember(Member member) {
	    // ✅ 1. 이메일 검증
	    if(member.getMemberEmail() == null || !member.getMemberEmail().contains("@")) {
	        throw new IllegalArgumentException("잘못된 이메일 형식입니다.");
	    }

	    // ✅ 2. 비밀번호 검증
	    if(!member.getMemberPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,20}$")) {
	        throw new IllegalArgumentException("비밀번호 형식이 올바르지 않습니다.");
	    }

	    // ✅ 3. 전화번호 검증
	    if(!member.getMemberPhone().matches("^\\d{3}-\\d{4}-\\d{4}$")) {
	        throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다.");
	    }

	    // ✅ 4. 이메일 중복 체크 (필수)
	    Member exist = memberMapper.getMemberFromEmail(member.getMemberEmail());
	    if(exist != null) {
	        throw new IllegalStateException("이미 존재하는 이메일입니다.");
	    }			
		// 비밀번호 암호화 기능
		String encoded = passwordEncoder.encode(member.getMemberPassword());
		member.setMemberPassword(encoded);
		
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
