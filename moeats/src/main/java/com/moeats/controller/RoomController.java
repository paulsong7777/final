package com.moeats.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/room")
public class RoomController {

	/*
	
	// 주문방 생성화면 테스트용
    @GetMapping({"/", "/create"})
    public String createForm(Model model, HttpSession session) {
    	
    	// 헤더 에러 방지를 위해 세션에 더미 로그인 정보 주입
        if (session.getAttribute("loginMember") == null) {
            Map<String, Object> loginMember = new HashMap<>();
            loginMember.put("memberIdx", 1L);
            loginMember.put("name", "김방장"); // 헤더의 ${session.loginMember.name}과 매칭
            loginMember.put("memberName", "김방장"); // (혹은 memberName일수도 있음)
            
            session.setAttribute("loginMember", loginMember);
        }
    	
    	List<Map<String, Object>> addressList = new ArrayList<>();
        
        Map<String, Object> addr1 = new HashMap<>();
        addr1.put("deliveryAddressIdx", 1L);
        addr1.put("addressLabel", "우리집");
        addr1.put("roadAddress", "서울시 강남구 테헤란로 123");
        addressList.add(addr1);

        Map<String, Object> addr2 = new HashMap<>();
        addr2.put("deliveryAddressIdx", 2L);
        addr2.put("addressLabel", "회사");
        addr2.put("roadAddress", "경기도 성남시 분당구 판교역로 456");
        addressList.add(addr2);        

        // 모델에 담아서 HTML로 전달
        model.addAttribute("addressList", addressList);
    	
        return "room/room-create";  // templates/room/room-create.html
    }
    
    // 주문방 협업화면(방장 직행) 테스트용 사전로직 : 
    @PostMapping("/detailForm") // 기존 생성 폼이 던지는 곳
    public String detailForm(HttpSession session, RedirectAttributes attr,
    		@RequestParam("storeIdx") Long storeIdx, 
    		@RequestParam("deliveryAddressIdx") Long deliveryAddressIdx,     		
            @RequestParam("paymentMode") String paymentMode,
            @RequestParam(value = "maxParticipants", required = false) Integer maxParticipants) {
    	
    	// 1. [진짜 로직 예정지] 여기서 DB에 INSERT를 수행하고 방 코드를 생성함
        String generatedCode = "A7B2X9"; 

        // 2. [테스트용 데이터 전달] 
        // 리다이렉트 시 세션에 잠깐 담았다가 바로 사라지는 FlashAttributes 사용
        attr.addFlashAttribute("storeIdx", storeIdx);
        attr.addFlashAttribute("deliveryAddressIdx", deliveryAddressIdx);
        attr.addFlashAttribute("paymentMode", paymentMode);
        attr.addFlashAttribute("maxParticipants", maxParticipants);
    	
    	return "redirect:/room/" + generatedCode + "/detailForm";
    }
    
    // 주문방 협업화면(방장 직행) 테스트용 실제로직 : 더미데이터 삽입
    @GetMapping("/room/{code}/detailForm")
    public String codeDetailForm(@PathVariable("code") String code, HttpSession session, Model model) {
    	
    	// 1. [참고] 위에서 rttr.addFlashAttribute로 담은 값들은 
        // 타임리프나 컨트롤러의 Model에 자동으로 들어와 있습니다. 
        // 만약 값이 없다면(참여자가 링크로 바로 들어온 경우) 기본값을 세팅해야 합니다.	
    	
    	
    	// 서버 콘솔에 찍어보기
        System.out.println(">>> 수신된 결제 방식: " + paymentMode); 
        System.out.println(">>> 수신된 배송지 IDX: " + deliveryAddressIdx);
    	
    	// 0. 하드코딩 사항 : 여기다가 새로 추가해야 할 내용은 싹 다 적어라
    	// 0-1. 방 정보 (room 객체)
        Map<String, Object> room = new HashMap<>();
        room.put("roomIdx", 100L);
        room.put("storeIdx", storeIdx);
        room.put("storeName", "비상구 치킨 본점");
        room.put("roomCode", "A7B2X9");
        room.put("roomStatus", "SELECTING"); // OPEN, SELECTING, PAYMENT_PENDING 등
        room.put("minimumOrderAmount", 20000);
        room.put("paymentMode", paymentMode);
        room.put("deliveryAddress", "서울시 강남구 테헤란로 123");
        model.addAttribute("room", room);

        // 0-2. 참여자 목록 (participants)
        List<Map<String, Object>> participants = new ArrayList<>();
        Map<String, Object> p1 = new HashMap<>();
        p1.put("memberName", "김방장");
        p1.put("role", "LEADER");
        p1.put("selectionStatus", "SELECTED");
        p1.put("cartTotal", 15000);
        participants.add(p1);
        
        Map<String, Object> p2 = new HashMap<>();
        p2.put("memberName", "이참여");
        p2.put("role", "PARTICIPANT");
        p2.put("selectionStatus", "NOT_SELECTED");
        p2.put("cartTotal", 8000);
        participants.add(p2);
        model.addAttribute("participants", participants);

        // 0-3. 내 장바구니 아이템 (myCartItems)
        List<Map<String, Object>> myCartItems = new ArrayList<>();
        Map<String, Object> item1 = new HashMap<>();
        item1.put("cartItemIdx", 1L);
        item1.put("menuName", "양념치킨");
        item1.put("quantity", 1);
        item1.put("unitPrice", 18000);
        item1.put("subtotal", 18000);
        myCartItems.add(item1);
        model.addAttribute("myCartItems", myCartItems);

        // 0-4. 기타 단일 변수들
        model.addAttribute("myTotal", 18000);
        model.addAttribute("mySelectionStatus", "NOT_SELECTED");
        model.addAttribute("myRole", "LEADER");
        model.addAttribute("totalMenuAmount", 23000);
        model.addAttribute("deliveryFee", 3000);
        model.addAttribute("grandTotal", 26000);
        model.addAttribute("allSelected", false);
    	
    	// 1. 하드코딩된 더미 데이터와 섞어서 출력하기
        System.out.println("========= [테스트 데이터 수신] =========");
        System.out.println("가게 번호: " + storeIdx);
        System.out.println("배송지 번호: " + deliveryAddressIdx);
        System.out.println("결제 방식: " + paymentMode);
        System.out.println("최대 인원: " + (maxParticipants == null ? "제한없음" : maxParticipants));
        
        // 2. 세션 대신 하드코딩된 방장 정보 출력
        System.out.println("가짜 방장 ID: user_admin_01");
        System.out.println("가짜 방 생성 시각: 2024-05-20 14:00:00");
        System.out.println("=====================================");
        
        // 3. [중요] 세션에 로그인한 사용자(방장) 정보가 없으면 가짜로 넣어줍니다.
        if (session.getAttribute("loginMember") == null) {
            Map<String, Object> loginMember = new HashMap<>();
            loginMember.put("memberIdx", 1L);      // 세션 내부의 memberIdx
            loginMember.put("name", "김방장");   // 헤더에서 요구하는 .name 
            loginMember.put("memberName", "김방장"); // (혹은 memberName일수도 있음)
            
            session.setAttribute("loginMember", loginMember);
        }
        
        
    	
    	return "room/room-detail";	// templates/room/room-detail.html
    }
    
    // 참여페이지(초대받는사람, 코드입력화면) 테스트용
    @GetMapping("/join")
    public String joinForm(HttpSession session, Model model) {
        // [테스트용] 제3자(참여자) 세션 주입
        // 기존 방장(1L), 기존 참여자(2L)과 다른 3L 번호를 가진 참여자로 설정
        if (session.getAttribute("loginMember") == null) {
            Map<String, Object> loginMember = new HashMap<>();
            loginMember.put("memberIdx", 3L); 
            loginMember.put("name", "삼꼽사리"); // 헤더 표시용
            session.setAttribute("loginMember", loginMember);
        }

        // 초기 화면에는 방 정보가 없으므로 null 처리
        model.addAttribute("roomCode", null);
        model.addAttribute("room", null);
        return "room/room-join";
    }

    // 코드를 입력하고 '참여하기' 버튼을 눌렀을 때
    @PostMapping("/{code}/join")
    public String processJoin(@PathVariable("code") String code, HttpSession session) {
        // 실제로는 여기서 DB에 참여 정보를 INSERT 합니다.
        System.out.println(">>> 주문방 참여 시도 코드: " + code);
        
        // 상세 페이지로 이동 (코드 값을 경로로 전달)
        return "redirect:/rooms/" + code + "/detail";
    }
    
    */
    
}
