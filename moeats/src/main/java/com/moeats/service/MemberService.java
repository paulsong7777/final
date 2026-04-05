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

        public void updateMember(Member member) {
                if (member.getMemberPassword() != null && !member.getMemberPassword().isEmpty()) {
                        String encoded = passwordEncoder.encode(member.getMemberPassword());
                        member.setMemberPassword(encoded);
                }

                if (member.getMemberPhone() != null) {
                        member.setMemberPhone(normalizePhone(member.getMemberPhone()));
                }

                memberMapper.updateMember(member);
        }

        public boolean isPassCheck(int member_idx, String member_password) {
                String dbpass = memberMapper.isPassCheck(member_idx);
                return passwordEncoder.matches(member_password, dbpass);
        }

        public Member login(String member_email, String member_password) {
                Member member = memberMapper.getMemberFromEmail(member_email);

                if (member == null) {
                        return null;
                }

                if (passwordEncoder.matches(member_password, member.getMemberPassword())) {
                        return member;
                }
                return null;
        }

        public void insertMember(Member member) {
                if (member.getMemberEmail() == null || !member.getMemberEmail().contains("@")) {
                        throw new IllegalArgumentException("잘못된 이메일 형식입니다.");
                }

                if (member.getMemberPassword() == null ||
                        !member.getMemberPassword().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_]).{8,20}$")) {
                        throw new IllegalArgumentException("비밀번호 형식이 올바르지 않습니다.");
                }

                member.setMemberPhone(normalizePhone(member.getMemberPhone()));

                Member exist = memberMapper.getMemberFromEmail(member.getMemberEmail());
                if (exist != null) {
                        throw new IllegalStateException("이미 존재하는 이메일입니다.");
                }

                if (member.getMemberRoleType() == null || member.getMemberRoleType().isBlank()) {
                        member.setMemberRoleType("USER");
                }

                if (member.getMemberStatus() == null || member.getMemberStatus().isBlank()) {
                        member.setMemberStatus("ACTIVE");
                }

                if (member.getDefaultDeliveryAddressIdx() != null && member.getDefaultDeliveryAddressIdx() == 0) {
                        member.setDefaultDeliveryAddressIdx(null);
                }

                String encoded = passwordEncoder.encode(member.getMemberPassword());
                member.setMemberPassword(encoded);

                memberMapper.insertMember(member);
        }

        public Member getMemberFromEmail(String member_email) {
                return memberMapper.getMemberFromEmail(member_email);
        }

        public Member getMember(int member_idx) {
                return memberMapper.getMember(member_idx);
        }

        private String normalizePhone(String rawPhone) {
                if (rawPhone == null) {
                        throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다.");
                }

                String digits = rawPhone.replaceAll("[^0-9]", "");

                if (digits.length() != 11) {
                        throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다.");
                }

                return digits.substring(0, 3) + "-" + digits.substring(3, 7) + "-" + digits.substring(7);
        }
}
