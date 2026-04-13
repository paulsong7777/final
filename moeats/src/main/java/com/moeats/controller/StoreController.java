package com.moeats.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.GroupOrder;
import com.moeats.domain.Member;
import com.moeats.domain.Store;
import com.moeats.dto.StoreSearchCond;
import com.moeats.geo.GeoPoint;
import com.moeats.geo.GeoService;
import com.moeats.service.StoreService;
import com.moeats.services.GroupOrderService;
import com.moeats.services.GroupOrderService.GroupOrderRecord;
import com.moeats.services.OrderMemberQueryService;
import com.moeats.services.OrderRoomService;
import com.moeats.services.sse.SSEService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class StoreController {
    @Autowired
    private StoreService storeService;
    @Autowired
    private GroupOrderService groupOrderService;
    @Autowired
    private SSEService sseService;
    @Autowired
    private GeoService geoService;
    @Autowired
    private OrderRoomService orderRoomService;
    
    @Autowired
    private OrderMemberQueryService orderMemberQueryService;
    
    private final String COMPLETED = "COMPLETED";
 // tier1에서 delivery 구현이 안되는 상황이기 때문에 간소화. 영훈
    private final List<String> ORDER_STATUSES =
            List.of("PAID", "ACCEPTED", "PREPARING", "READY", "DELIVERING", "COMPLETED");
//    private final List<String> ORDER_STATUSES = List.of("PAID","ACCEPTED","PREPARING","READY","DELIVERING","COMPLETED");
    
    
    @ModelAttribute("store")
    public Store getStore(@SessionAttribute(name = "member", required = false) Member member) {
        if (member != null && "OWNER".equals(member.getMemberRoleType())) {
            Store store = storeService.myStore(member.getMemberIdx());
            if (store != null) {
                return store;
            }
        }
        // 💡 핵심 수정: null 대신 '빈 Store 객체'를 반환해야
        // 스프링이 사용자가 폼에 입력한 데이터를 이 객체에 차곡차곡 담아줍니다!
        return new Store();
    }
    
    
    // 가게 전체 조회
    @GetMapping("/stores")
    @ResponseBody
    public List<Store> storeList(StoreSearchCond cond, int memberIdx) {
        return storeService.getStoreList(cond, memberIdx);
    }
    
	// 대시보드
	@GetMapping("/owners/dashboard")
	public String dashboard(
			RedirectAttributes ra,
			Model model,
			@SessionAttribute("member") Member member) {
		Store store = storeService.myStore(member.getMemberIdx());
	    if ( store==null ) {
	    	ra.addFlashAttribute("error", "가게가 없습니다");
	    	return "redirect:/owners/store/new";
	    }
	    List<GroupOrderRecord> groupOrderList = groupOrderService.findRecordByStore(store.getStoreIdx());
        
	    // 방장들의 Nickname을 맵으로 추출
	    Map<Integer, String> leaderNameMap = orderMemberQueryService.findByIdxs(
	        groupOrderList.stream().map(r -> r.groupOrder().getLeaderMemberIdx()).toList()
	    ).stream().collect(Collectors.toMap(Member::getMemberIdx, Member::getMemberNickname));

	    model.addAttribute("leaderNameMap", leaderNameMap);
	    model.addAttribute("orderList", groupOrderList);
	    
	    model.addAttribute("menu", "dash");
	    model.addAttribute("store", store);
	    model.addAttribute("orderList", groupOrderList);
	    return "views/owner/dashboard";
	}
	
	@GetMapping("/owners/order/list")
	public String orderList(
			RedirectAttributes ra,
			Model model,
			@SessionAttribute("member") Member member) { 
		Store store = storeService.myStore(member.getMemberIdx());
		if ( store==null ) {
			ra.addFlashAttribute("error", "가게가 없습니다");
			return "redirect:/owners/store/new";
		}
		List<GroupOrderRecord> groupOrders = groupOrderService.findRecordByStore(store.getStoreIdx());
		
		model.addAttribute("menu", "order-list");
		model.addAttribute("store", store);
		model.addAttribute("groupOrders", groupOrders);
		return "views/owner/order-list"; 
	}
	
	@GetMapping("/owners/order/detail")
	public String orderDetail(
            RedirectAttributes ra,
            Model model,
            @RequestParam(name = "roomIdx", defaultValue = "0") int roomIdx,
            @SessionAttribute("member") Member member) {

        Store store = storeService.myStore(member.getMemberIdx());
        if (store == null) {
            ra.addFlashAttribute("error", "가게가 없습니다");
            return "redirect:/owners/store/new";
        }

        if (roomIdx == 0) {
            ra.addFlashAttribute("error", "잘못된 주문 번호입니다");
            return "redirect:/owners/dashboard";
        }

        GroupOrder groupOrder = groupOrderService.findByRoom(roomIdx);
        if (groupOrder == null
                || groupOrder.getStoreIdx() != store.getStoreIdx()
                || !ORDER_STATUSES.contains(groupOrder.getOrderStatus())) {
            ra.addFlashAttribute("error", "잘못된 주문 번호입니다");
            return "redirect:/owners/dashboard";
        }

        GroupOrderService.GroupOrderRecord orderRecord =
                groupOrderService.findRecordByIdx(groupOrder.getOrderIdx());

        // 🌟 [핵심] 주문한 사람들의 닉네임을 가져와서 화면으로 넘겨주는 로직 🌟
        try {
            List<Integer> memberIdxList = orderRecord.groupOrderItems().stream()
                    .map(com.moeats.domain.GroupOrderItem::getMemberIdx)
                    .distinct()
                    .toList();

            Map<Integer, String> memberNameMap = orderMemberQueryService.findByIdxs(memberIdxList).stream()
                    .collect(Collectors.toMap(Member::getMemberIdx, Member::getMemberNickname));

            model.addAttribute("memberNameMap", memberNameMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 	orderDetail 메서드 내부
        Member leader = orderMemberQueryService.findByIdxs(List.of(groupOrder.getLeaderMemberIdx())).get(0);
        model.addAttribute("leader", leader);

        model.addAttribute("menu", "order-detail");
        model.addAttribute("store", store);
        model.addAttribute("orderRecord", orderRecord);
        model.addAttribute("orderRoom", orderRoomService.findByIdx(groupOrder.getRoomIdx()));
        return "views/owner/order-detail";
    }
    // 3차 디버깅 결과 터질 위험요소 발견. ViewOwnerController 삭제로 보강 필요하다 판단.
//    @PostMapping("/order/status_update")
//    @ResponseBody
//    public Map statusMove(
//    		@RequestParam(name = "roomIdx",defaultValue = "0") int roomIdx,
//			@SessionAttribute("member") Member member) {
//    	Store store = storeService.myStore(member.getMemberIdx());
//    	if ( store==null || roomIdx==0 ) {
//    		return Map.of("result",false);
//    	}
//    	GroupOrder groupOrder = groupOrderService.findByRoom(roomIdx);
//    	if( 	groupOrder==null
//    			|| groupOrder.getStoreIdx() != store.getStoreIdx()
//    			|| !ORDER_STATUSES.contains(groupOrder.getOrderStatus())
//    			|| COMPLETED.equals(store.getStoreStatus())
//    			|| groupOrderService.proceed(groupOrder) == 0 ) {
//    		return Map.of("result",false);
//    	}
//    	sseService.statusChangeOrder(groupOrder.getOrderIdx(), ORDER_STATUSES.get(ORDER_STATUSES.indexOf(groupOrder.getOrderStatus())+1));
//    	return Map.of("result",true);
//    }

	
	@PostMapping({"/owners/order/status_update", "/owner/status_update"})
	@ResponseBody
	public Map<String, Object> statusMove(
	        @RequestBody Map<String, Object> body,
	        @SessionAttribute("member") Member member) {

	    Object roomIdxObj = body.get("roomIdx");
	    int roomIdx = roomIdxObj == null ? 0 : Integer.parseInt(String.valueOf(roomIdxObj));

	    Store store = storeService.myStore(member.getMemberIdx());
	    if (store == null || roomIdx == 0) {
	        return Map.of("result", false);
	    }

	    GroupOrder groupOrder = groupOrderService.findByRoom(roomIdx);
	    if (groupOrder == null
	            || groupOrder.getStoreIdx() != store.getStoreIdx()
	            || !ORDER_STATUSES.contains(groupOrder.getOrderStatus())
	            || groupOrderService.proceed(groupOrder) == 0) {
	        return Map.of("result", false);
	    }

	    GroupOrder updated = groupOrderService.findByRoom(roomIdx);
	    sseService.statusChangeOrder(updated.getOrderIdx(), updated.getOrderStatus());

	    return Map.of(
	            "result", true,
	            "orderStatus", updated.getOrderStatus(),
	            "roomIdx", updated.getRoomIdx(),
	            "orderIdx", updated.getOrderIdx()
	    );
	}
	
	
    // 가게 상태 수정
//    @PostMapping("/owners/store/status")
//    public String updateStatus(
//    		RedirectAttributes ra,
//    		@RequestParam("storeIdx") int storeIdx, 
//    		@RequestParam("storeStatus") String storeStatus,
//			@SessionAttribute("member") Member member) {
//    	Store store = storeService.myStore(member.getMemberIdx());
//    	if ( store==null ) {
//    		ra.addFlashAttribute("error", "잘못된 접근입니다");
//    		return "redirect:/home";
//    	}
//    	
//        storeService.updateStatus(storeIdx, member.getMemberIdx(), storeStatus);
//        return "redirect:"+ redirectUrl;
//    }
    // 재우야 ... 시계를 푸르며
    @PostMapping("/owners/store/status")
    public String updateStatus(
            RedirectAttributes ra,
            @RequestParam("storeIdx") int storeIdx,
            @RequestParam("storeStatus") String storeStatus,
            @RequestParam(value = "redirectUrl", defaultValue = "/owners/store") String redirectUrl,
            @SessionAttribute(name = "member") Member member) {

        Store store = storeService.myStore(member.getMemberIdx());
        if (store == null) {
            ra.addFlashAttribute("error", "잘못된 접근입니다");
            return "redirect:/home";
        }

        storeService.updateStatus(storeIdx, member.getMemberIdx(), storeStatus);
        return "redirect:" + redirectUrl;
    }
    
    

 // 가게 정보 수정
    @PostMapping("/owners/store/edit")
    public String updateStore(
            RedirectAttributes ra,
            @ModelAttribute Store store,
            @RequestParam(value = "redirectUrl", defaultValue = "/owners/store") String redirectUrl, // ✨ 추가
            @SessionAttribute("member") Member member) {
        
        // 1. 기존 가게 정보 조회 및 권한 체크
        Store check = storeService.myStore(member.getMemberIdx());
        if ( check==null || check.getStoreIdx()!=store.getStoreIdx() ) {
            ra.addFlashAttribute("error", "잘못된 접근입니다");
            return "redirect:/home";
        }
        
        // 💡 2. [핵심] 주소가 변경되었을 수 있으므로 GeoService를 통해 좌표 다시 계산
        if (store.getStoreAddress1() != null && !store.getStoreAddress1().isBlank()) {
            try {
                GeoPoint point = geoService.getLatLng(store.getStoreAddress1());
                store.setLatitude(point.getLat());
                store.setLongitude(point.getLng());
                log.info("수정된 주소 좌표 변환 성공: 위도 {}, 경도 {}", point.getLat(), point.getLng());
            } catch (Exception e) {
                log.error("수정된 주소 좌표 변환 실패: {}", store.getStoreAddress1(), e);
                // 변환 실패 시, 안전하게 기존(check) 좌표를 그대로 유지하도록 세팅
                store.setLatitude(check.getLatitude());
                store.setLongitude(check.getLongitude());
            }
        }
        
        store.setOwnerMemberIdx(member.getMemberIdx());
        storeService.updateStore(store);
        
        return "redirect:" + redirectUrl; 
    }

    // 가게 정보 수정 폼
    @GetMapping("/owners/store/edit")
    public String updateStore(
    		RedirectAttributes ra,
    		Model model,
    		@RequestParam(value = "redirectUrl", defaultValue = "/owners/store") String redirectUrl, // ✨ 추가
			@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if ( store==null ) {
    		ra.addFlashAttribute("error", "가게가 없습니다.");
    		return "redirect:/owners/store/new";
    	}

        model.addAttribute("menu", "store");
        model.addAttribute("store", store);
        model.addAttribute("redirectUrl", redirectUrl); // ✨ 추가 (View로 전달)
        return "views/owner/store-edit";
    }

    // 가게 등록
    @PostMapping("/owners/store")
    public String insertStore(
            @ModelAttribute("store") Store store, 
            @RequestParam(value="storeImgFile", required=false) MultipartFile storeImgFile,
            @SessionAttribute("member") Member member) {
        
        if (store == null) {
            store = new Store(); 
        }
        
        store.setOwnerMemberIdx(member.getMemberIdx());
        
        // 💡 2. [핵심] DB 저장 전, GeoService를 호출하여 주소를 좌표로 변환!
        if (store.getStoreAddress1() != null && !store.getStoreAddress1().isBlank()) {
            try {
                // 도로명 주소(storeAddress1)를 넘겨서 좌표를 받아옴
                GeoPoint point = geoService.getLatLng(store.getStoreAddress1());
                
                // 변환된 위도/경도를 store 객체에 세팅 (HTML에서 넘어온 서울 좌표를 덮어씌움)
                store.setLatitude(point.getLat());
                store.setLongitude(point.getLng());
                
                log.info("좌표 변환 성공: 위도 {}, 경도 {}", point.getLat(), point.getLng());
            } catch (Exception e) {
                // 주소 변환 실패 시 로그 기록 (대구 등지에서 못 찾는 주소일 경우 대비)
                log.error("주소 좌표 변환 실패: {}", store.getStoreAddress1(), e);
            }
        }
        
        // 올바른 좌표가 세팅된 store를 DB에 저장
        storeService.insertStore(store);

        return "redirect:/owners/store";
    }

    // 가게 등록 폼
    @GetMapping("/owners/store/new")
    public String insertStore(
    		Model model) {
        model.addAttribute("menu", "store-new");
        return "views/owner/store-create";
    }
    
    // 내 가게 조회
    @GetMapping("/owners/store")
    public String myStore(
    		RedirectAttributes ra,
    		Model model,
			@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if ( store==null ) {
    		ra.addFlashAttribute("error", "가게가 없습니다.");
    		return "redirect:/owners/store/new";
    	}
    	
    	model.addAttribute("store", store);
    	
    	return "views/owner/store-manage";
    }
    
    // 9. 리뷰 답변 관리
    @GetMapping({"/owners/review/management","/owner/review/management"})
    public String reviewManagement(Model model) {
    	log.info("미구현");
        model.addAttribute("menu", "review");
        return "views/owner/review-management";
    }

    // 10. 영업 리포트
    @GetMapping({"/owners/sales/report","/owner/sales/report"})
    public String salesReport(Model model) {
    	log.info("미구현");
        model.addAttribute("menu", "report");
        return "views/owner/sales-report";
    }
    
    @GetMapping("/owners/order/api/list")
    @ResponseBody
    public List<GroupOrderService.GroupOrderRecord> orderListApi(
            @SessionAttribute("member") Member member) {
        Store store = storeService.myStore(member.getMemberIdx());
        if (store == null) return List.of();
        
        // DB에서 주문 목록을 가져옵니다. 
        // (정렬은 이전에 XML에서 수정한 대로 미완료 주문 우선으로 나옵니다.)
        return groupOrderService.findRecordByStore(store.getStoreIdx());
    }
    
    @GetMapping(value = "/owners/api/sse/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public SseEmitter connectStoreSse(@SessionAttribute("member") Member member) {
        Store store = storeService.myStore(member.getMemberIdx());
        if (store == null) {
            return null;
        }
        // SSEService의 storeMap에 현재 사장님의 매장 번호로 구독(연결)을 시작합니다.
        return sseService.joinStore(store.getStoreIdx());
    }
}