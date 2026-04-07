package com.moeats.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.moeats.service.MemberAccountService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@SessionAttributes("member")	// model에 담긴 member를 session에도 자동으로 저장
@Slf4j
public class MemberController {
	
	@Autowired
	private MemberAccountService memberService;
	
	// ===== 상수 정의 ======
	private static final String ROLE_OWNER = "OWNER";
	private static final String ROLE_USER = "USER";
	
	
	
	// 이메일 중복 확인 true → 사용 가능 (중복 없음)	false → 이미 존재
	@GetMapping("/members/email-check")
	@ResponseBody
	public boolean checkEmail(@RequestParam("memberEmail") String memberEmail) {
		return memberService.getMemberFromEmail(memberEmail) == null;
	}
	
	
	// 회원 수정
	@PostMapping("/members/me/edit")
	public String updateMember(Member member,
			Model model,
			RedirectAttributes ra,
			@SessionAttribute("member") Member loginUser,
			@RequestParam("memberPassword") String memberPassword) {
		
		boolean isPassCheck = memberService.isPassCheck(loginUser.getMemberIdx(), memberPassword);
		
		if(!isPassCheck) {
			model.addAttribute("error", "비밀번호를 확인해주세요.");
			model.addAttribute("member", member);
			return "views/members/member-profile-edit";
		}
		
		member.setMemberIdx(loginUser.getMemberIdx());
		memberService.updateMember(member);
		
		return "redirect:/members/me";
	}
	
	
	
	// 회원 수정 폼 요청
	@GetMapping("/members/me/edit")
	public String updateMemberForm(@SessionAttribute(name="member", required=false) Member member) {
		if(member==null) {
			return "redirect:/login";
		}
		
		return "views/members/member-profile-edit";
	}
	
	// 마이페이지 띄우기 폼
	@GetMapping("/members/me")
	public String myPage(@SessionAttribute(name="member", required=false) Member member) {

		return "views/members/member-profile";
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
		
		// --- ✅ 로그인 성공 처리 ---
		model.addAttribute("member", member);
		session.setAttribute("memberIdx", member.getMemberIdx());
		
		// 💡 핵심 추가 로직: 인터셉터가 세션에 남겨둔 '원래 가려던 주소'를 꺼내옵니다.
		String redirectURI = (String) session.getAttribute("redirectURI");
		
		if (redirectURI != null && !redirectURI.isEmpty()) {
			// 다 쓴 주소 메모는 세션에서 깔끔하게 지워줍니다.
			session.removeAttribute("redirectURI"); 
			
			// 원래 가려던 페이지로 스무스하게 보냅니다!
			return "redirect:" + redirectURI;
		}

	    // 사업자 회원이면 사업자 대시보드로 리다이렉트
	    if (ROLE_OWNER.equals(member.getMemberRoleType())) {
	        return "redirect:/members/dashboard";
	    }
	    
		// 만약 가려던 주소가 없었다면(그냥 로그인 버튼 누르고 들어온 경우) 메인으로 보냅니다.
		return "redirect:/main";		
	}

	// 로그인 폼
	@GetMapping("/login")
	public String login() {
		
		return "views/members/login";
	}
	
	// 회원가입	- 일반/사업자 분기
	@PostMapping("/members")
	public String insertMember(Member member, RedirectAttributes ra) {

	    try {
	        memberService.insertMember(member);
	        return "redirect:/login";

	    } catch (Exception e) {
	        ra.addFlashAttribute("error", e.getMessage());
	        return "redirect:/members/createType";
	    }
	}
	
	// 통합 대시보드 분기(역할분기 일반/사업자)
	@GetMapping("/members/dashboard")
	public String dashboard(Model model,@SessionAttribute("member") Member member) {
		
	    if (ROLE_OWNER.equals(member.getMemberRoleType())) {
		        model.addAttribute("menu", "dash");

		        // 실시간 주문 가상 데이터 생성
		        List<Map<String, Object>> list = new ArrayList<>();
		        Map<String, Object> o = new HashMap<>();
		        o.put("roomIdx", 20260406001L);
		        o.put("roomStatus", "WAITING");
		        o.put("orderMenu", "스테이크 비빔밥 외 1건");
		        o.put("totalPrice", 24000);
		        o.put("orderTime", "18:50");
		        list.add(o);

		        // HTML에서 어떤 이름을 쓰든 걸리도록 다 넣어줍니다.
		        model.addAttribute("activeOrders", list);
		        model.addAttribute("orders", list);
		        model.addAttribute("orderList", list);
		        //System.out.println(member);
		        log.info("/members/dashboard: {}", member);
		        
	        return "views/owner/dashboard";
	    }

	    return "redirect:/main";
	}
	
	// 회원가입 폼 - 사업자
	@GetMapping("/members/new-owner")
	public String insertMemberOwner() {
		
		return "views/members/auth-signup-owner";
	}

	// 회원가입 폼 - 일반
	@GetMapping("/members/new-user")
	public String insertMemberUSER() {
		
		return "views/members/auth-signup-user";
	}

	// 회원가입 유형 선택 폼(일반/사업자)
	@GetMapping("/members/createType")
	public String createType() {
		
		return "views/members/create-type";
	}
	
}
