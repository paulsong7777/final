package com.moeats.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.DeliveryAddress;
import com.moeats.domain.Member;
import com.moeats.domain.Store;
import com.moeats.service.DeliveryAddressService;
import com.moeats.service.MemberAccountService;
import com.moeats.service.StoreService;
import com.moeats.services.GroupOrderService;
import com.moeats.services.GroupOrderService.GroupOrderRecord;
import com.moeats.services.sse.SSEService;

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
	
	@Autowired
	private GroupOrderService groupOrderService;
	
    @Autowired
    private SSEService sseService;
	
	
	// ===== 상수 정의 ======
	private static final String ROLE_OWNER = "OWNER";
	private static final String ROLE_USER = "USER";
	
	@GetMapping("/members/me/orders")
	public String myOrderHistory(@SessionAttribute(name="member", required=false) Member member,
			Model model) {
		
		// USER용 기본 배송지
		if("USER".equals(member.getMemberRoleType())) {
		    DeliveryAddress deliveryAddress = deliveryAddressService.getDefaultAddress(member.getMemberIdx());
		    model.addAttribute("deliveryAddress", deliveryAddress);
		    
		    // 전체 주소 리스트 추가
		    List<DeliveryAddress> addressList =
		            deliveryAddressService.getAddress(member.getMemberIdx());
		    
		    List<GroupOrderRecord> orderList = groupOrderService.findRecentOrdersByMember(member.getMemberIdx());
		    model.addAttribute("orderList", orderList);
		    
		    model.addAttribute("addressList", addressList);
		}
		return "views/user/user-order-history";
	}
	
	/**
     * 마이페이지 내역 클릭 시 상세 데이터를 JSON으로 반환
     */
    @GetMapping("members/me/api/orders/{orderIdx}")
    @ResponseBody
    public GroupOrderService.GroupOrderRecord getOrderApi(@PathVariable("orderIdx") int orderIdx) {
        // GroupOrderRecord는 GroupOrderService 내부의 record/static class일 것이므로 
        // 서비스명을 붙여서 참조하거나 import 해야 합니다.
        return groupOrderService.findRecordByIdx(orderIdx);
    }
	
	// 이메일 중복 확인 true → 사용 가능 (중복 없음)	false → 이미 존재
	@GetMapping("/members/email-check")
	@ResponseBody
	public boolean checkEmail(@RequestParam("memberEmail") String memberEmail) {
		return memberService.getMemberFromEmail(memberEmail) == null;
	}
	
	// 비밀번호 실시간 확인 AJAX용
	@PostMapping("/members/password-check")
	@ResponseBody
	public boolean checkPasswordAjax(
	        @RequestParam("memberPassword") String memberPassword,
	        @SessionAttribute("member") Member loginUser) {
	    
	    // 서비스의 isPassCheck를 호출하여 DB의 암호화된 비번과 비교
	    return memberService.isPassCheck(loginUser.getMemberIdx(), memberPassword);
	}
	
	// 회원 수정
	@PostMapping("/members/me/edit")
	public String updateMember(Member member,
			Model model,
			RedirectAttributes ra,
			@SessionAttribute("member") Member loginUser,
			@RequestParam("memberPassword") String memberPassword) {
		
		// 1. 현재 비밀번호(oldPassword)가 DB와 일치하는지 먼저 검사
	    boolean isPassCheck = memberService.isPassCheck(loginUser.getMemberIdx(), memberPassword);
	    
	    if(!isPassCheck) {
	        // ❌ 틀렸을 경우: 다시 수정 페이지로 보내면서 에러 메시지 전달
	        ra.addFlashAttribute("error", "현재 비밀번호가 일치하지 않습니다.");
	        return "redirect:/members/me/edit";
	    }
	    
	    // 2. 맞았을 경우: 정보 업데이트 진행
	    // (이때 member 객체 안의 memberPassword는 '새로 변경할 비밀번호'가 담겨 있어야 함)
	    member.setMemberIdx(loginUser.getMemberIdx());
	    memberService.updateMember(member);
	    
	    ra.addFlashAttribute("message", "정보가 수정되었습니다.");
	    return "redirect:/members/me";
	}
	
	
	
	// 회원 수정 폼 요청	sessionMember - 등록된 회원 정보
	@GetMapping("/members/me/edit")
	public String updateMemberForm(@SessionAttribute(name="member", required=false) Member sessionMember
			,Model model		) {
		if(sessionMember==null) {
			return "redirect:/login";
		}
		// ⭐ 세션의 이메일을 이용해 DB에서 '최신 회원 정보'를 다시 조회합니다.
		Member latestMember = memberService.getMemberFromEmail(sessionMember.getMemberEmail());
		
		// ⭐ 조회한 최신 정보를 'member'라는 이름으로 모델에 담아 화면에 넘깁니다.
		model.addAttribute("member", latestMember);
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
	        
	        List<GroupOrderRecord> orderList = groupOrderService.findRecentOrdersByMember(member.getMemberIdx());
	        model.addAttribute("orderList", orderList);
	        
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
	
	// 회원가입 - 일반/사업자 분기
		@PostMapping("/members")
		public String insertMember(@ModelAttribute("newMember") Member member, RedirectAttributes ra, HttpSession session) {
	        // 💡 @ModelAttribute("newMember")를 붙여서
	        // 스프링이 이 객체를 "member"가 아닌 "newMember"로 인식하게 만듭니다.
	        // 이렇게 하면 @SessionAttributes("member")가 반응하지 않습니다!

			try {
				memberService.insertMember(member);

				session.invalidate(); // ⭐ 기존 로그인 세션 제거

				return "redirect:/login";

			} catch (Exception e) {
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
	        model.addAttribute("store", store);

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
	
	// 🌟 유저 실시간 주문 상태 확인용 SSE 연결 API 🌟
		@GetMapping(value = "/members/api/sse/orders/{orderIdx}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
		@ResponseBody
		public SseEmitter connectOrderSse(@PathVariable("orderIdx") int orderIdx) {
		    // SSEService의 orderMap에 이 주문 번호로 구독을 시작합니다.
		    return sseService.joinOrder(orderIdx);
		}
	
}
