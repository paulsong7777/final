package com.moeats.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.Member;
import com.moeats.service.MemberService;

import jakarta.servlet.http.HttpSession;

@Controller
@SessionAttributes("member")	// model에 담긴 member를 session에도 자동으로 저장
public class MemberController {
	
	@Autowired
	private MemberService memberService;
	
	
	
	
	// 회원 수정
	@PostMapping("/member/me/edit")
	public String updateMember(Member member,
			Model model,
			RedirectAttributes ra,
			@SessionAttribute("member") Member loginUser,
			@RequestParam("member_password") String member_password) {
		
		boolean isPassCheck = memberService.isPassCheck(loginUser.getMember_idx(), member_password);
		
		if(!isPassCheck) {
			model.addAttribute("error", "비밀번호를 확인해주세요.");
			model.addAttribute("member", member);
			return "views/member-profile-edit";
		}
		
		member.setMember_idx(loginUser.getMember_idx());
		memberService.updateMember(member);
		
		return "redirect:/member/me";
	}
	
	
	
	// 회원 수정 폼 요청
	@GetMapping("/member/me/edit")
	public String updateMemberForm(@SessionAttribute(name="member", required=false) Member member) {
		if(member==null) {
			return "redirect:/login";
		}
		
		return "views/member-profile-edit";
	}
	
	// 마이페이지 띄우기 폼
	@GetMapping("/member/me")
	public String myPage(@SessionAttribute(name="member", required=false) Member member) {
		if(member == null ) {
			return "redirect:/login";
		}
		return "views/member-profile";
	}
	
	// 로그아웃 처리
	@PostMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/main";
	}
	
	// 로그인 처리
	@PostMapping("/login")
	public String login(Model model,
					@RequestParam("member_email") String memberEmail,
					@RequestParam("member_password") String memberPassword,
					RedirectAttributes ra) throws Exception {
		
		Member member = memberService.login(memberEmail, memberPassword);
		
		if(member == null) {
			ra.addFlashAttribute("error", "존재하지 않는 아이디 입니다.");
			return "redirect:/login";
			
		}
		
		model.addAttribute("member", member);
		return "redirect:/main";		
	}
	
	// 로그인 폼
	@GetMapping("/login")
	public String login() {
		
		return "views/login";
	}
	
	// 회원가입	- 일반/사업자 분기 or 회원가입 폼에서 체크박스로 분기
	@PostMapping("/member")
	public String insertMember(Member member) {
		
		memberService.insertMember(member);
		
		return "redirect:/login";
	}
	
	// 회원가입 폼
	@GetMapping("/members/new")
	public String insertMember() {
		
		return "views/auth-signup";
	}
}
