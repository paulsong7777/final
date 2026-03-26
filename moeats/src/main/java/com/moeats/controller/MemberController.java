package com.moeats.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.Member;
import com.moeats.service.MemberService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@SessionAttributes("member")	// model에 담긴 member를 session에도 자동으로 저장
public class MemberController {
	
	@Autowired
	private MemberService memberService;
	
	// 이메일 중복 확인 true → 사용 가능 (중복 없음)	false → 이미 존재
	@GetMapping("/member/email-check")
	@ResponseBody
	public boolean checkEmail(@RequestParam("memberEmail") String memberEmail) {
		return memberService.getMemberFromEmail(memberEmail) == null;
	}
	
	
	// 회원 수정
	@PostMapping("/member/me/edit")
	public String updateMember(Member member,
			Model model,
			RedirectAttributes ra,
			@SessionAttribute("member") Member loginUser,
			@RequestParam("memberPassword") String memberPassword) {
		
		boolean isPassCheck = memberService.isPassCheck(loginUser.getMemberIdx(), memberPassword);
		
		if(!isPassCheck) {
			model.addAttribute("error", "비밀번호를 확인해주세요.");
			model.addAttribute("member", member);
			return "views/member-profile-edit";
		}
		
		member.setMemberIdx(loginUser.getMemberIdx());
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
					@RequestParam("memberEmail") String memberEmail,
					@RequestParam("memberPassword") String memberPassword,
					HttpSession session,
					HttpServletResponse response,
					RedirectAttributes ra) throws Exception {
		
		Member member = memberService.login(memberEmail, memberPassword);
		
		if(member == null) {
			// 원인 분리
			Member findMember = memberService.getMemberFromEmail(memberEmail);
			
			if(findMember == null) {
				ra.addFlashAttribute("error", "존재하지 않는 아이디 입니다.");
			}else {
				ra.addFlashAttribute("error", "아이디 혹은 비밀번호를 확인해주세요.");
			}
			return "redirect:/login";
			
		}
		model.addAttribute("member", member);
		session.setAttribute("memberIdx", member.getMemberIdx());
		
		return "redirect:/main";		
	}
	
	// 로그인 폼
	@GetMapping("/login")
	public String login() {
		
		return "views/login";
	}
	
	// 회원가입	- 일반/사업자 분기 or 회원가입 폼에서 체크박스로 분기
	@PostMapping("/member")
	public String insertMember(Member member, RedirectAttributes ra) {

	    try {
	        memberService.insertMember(member);
	        return "redirect:/login";

	    } catch (IllegalArgumentException | IllegalStateException e) {
	        ra.addFlashAttribute("error", e.getMessage());
	        return "redirect:/members/new";
	    }
	}
	
	// 회원가입 폼
	@GetMapping("/members/new")
	public String insertMember() {
		
		return "views/auth-signup";
	}
}
