package com.moeats.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.DeliveryAddress;
import com.moeats.domain.Member;
import com.moeats.domain.Store;
import com.moeats.service.DeliveryAddressService;
import com.moeats.service.MemberAccountService;
import com.moeats.service.StoreService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@SessionAttributes("member")	// model에 담긴 member를 session에도 자동으로 저장
public class MemberController {
	
	@Autowired
	private MemberAccountService memberService;
	
	@Autowired
	private DeliveryAddressService deliveryAddressService;
	
	@Autowired
	private StoreService storeService;
	
	
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
	public String updateMemberForm(@SessionAttribute(name="member", required=false) Member member
					) {
		if(member==null) {
			return "redirect:/login";
		}
		
		return "views/members/member-profile-edit";
	}
	
	// 마이페이지 띄우기 폼
	@GetMapping("/members/me")
	public String myPage(@SessionAttribute(name="member", required=false) Member member,
					Model model) {
		
	    // USER용 기본 배송지
	    if("USER".equals(member.getMemberRoleType())) {
	        DeliveryAddress deliveryAddress = deliveryAddressService.getDefaultAddress(member.getMemberIdx());
	        model.addAttribute("deliveryAddress", deliveryAddress);
	        
	        // 전체 주소 리스트 추가
	        List<DeliveryAddress> addressList =
	                deliveryAddressService.getAddress(member.getMemberIdx());
	        model.addAttribute("addressList", addressList);
	    }

	    // OWNER용 가게 정보
	    if("OWNER".equals(member.getMemberRoleType())) {
	        Store store = storeService.myStore(member.getMemberIdx());
	        model.addAttribute("store", store);
	    }
	    
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
	        return "redirect:/owners/dashboard";
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
	public String insertMember(Member member, RedirectAttributes ra, 
            SessionStatus sessionStatus, HttpSession session) {

	    try {
	    	// 1. DB에 회원 정보 저장
	        memberService.insertMember(member);
	        
	        // 🔥 2. 해결 코드: @SessionAttributes에 의해 임시로 세션에 담긴 member 비우기
	        sessionStatus.setComplete(); 
	        
	        // 🔥 3. 혹시 모를 기존 찌꺼기 세션까지 완전히 날려버림 (안전장치)
	        session.invalidate();

	        // 4. 깨끗한 상태로 로그인 폼으로 이동
	        return "redirect:/login";

	    } catch (Exception e) {
	    	e.printStackTrace();
	        ra.addFlashAttribute("error", e.getMessage());
	        return "redirect:/members/createType";
	    }
	}
	
	// 통합 대시보드 분기(역할분기 일반/사업자)
	@GetMapping("/members/dashboard")
	public String dashboard(@SessionAttribute("member") Member member,
					Model model) {

	    if (ROLE_OWNER.equals(member.getMemberRoleType())) {
	    	// 1. 로그인한 점주의 가게 정보를 DB에서 조회
	        Store store = storeService.myStore(member.getMemberIdx());
	        
	        // 2. 화면(HTML)에서 사용하는 변수명인 'storeVo'로 Model에 담아 전달
	        model.addAttribute("storeVo", store);

            // 💡 참고: HTML을 보면 주문 내역(orderList)도 필요로 합니다.
            // 주문 내역을 가져오는 서비스가 있다면 아래처럼 같이 넘겨주어야 실시간 주문 현황도 뜹니다.
            // List<Order> orderList = orderService.getCurrentOrders(store.getStoreIdx());
            // model.addAttribute("orderList", orderList);
	    	
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
