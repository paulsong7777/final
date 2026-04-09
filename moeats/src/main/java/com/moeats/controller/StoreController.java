package com.moeats.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.GroupOrder;
import com.moeats.domain.Member;
import com.moeats.domain.Store;
import com.moeats.dto.StoreSearchCond;
import com.moeats.service.StoreService;
import com.moeats.services.GroupOrderService;
import com.moeats.services.GroupOrderService.GroupOrderRecord;
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
    
    private final String COMPLETED = "COMPLETED";
    private final List<String> ORDER_STATUSES = List.of("PAID","ACCEPTED","PREPARING","READY","DELIVERING","COMPLETED");
    
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
    	if ( store==null ) {
    		ra.addFlashAttribute("error", "가게가 없습니다");
    		return "redirect:/owners/store/new";
    	}
    	if( roomIdx==0 ) {
    		ra.addFlashAttribute("error","잘못된 주문 번호입니다");
    		return "redirect:/owners/dashboard";
    	}
    	GroupOrderRecord groupOrder = groupOrderService.findRecordByIdx(roomIdx);
    	if( 	groupOrder.groupOrder()==null
    			|| groupOrder.groupOrder().getStoreIdx() != store.getStoreIdx()
    			|| !ORDER_STATUSES.contains(groupOrder.groupOrder().getOrderStatus())) {
    		ra.addFlashAttribute("error","잘못된 주문 번호입니다");
    		return "redirect:/owners/dashboard";
    	}
        
    	model.addAttribute("menu", "order-detail");
    	model.addAttribute("store", store);
        model.addAttribute("groupOrder", groupOrder);
        return "views/owner/order-detail"; 
    }
    
    @PostMapping("/order/status_update")
    @ResponseBody
    public Map statusMove(
    		@RequestParam(name = "roomIdx",defaultValue = "0") int roomIdx,
			@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if ( store==null || roomIdx==0 ) {
    		return Map.of("result",false);
    	}
    	GroupOrder groupOrder = groupOrderService.findByRoom(roomIdx);
    	if( 	groupOrder==null
    			|| groupOrder.getStoreIdx() != store.getStoreIdx()
    			|| !ORDER_STATUSES.contains(groupOrder.getOrderStatus())
    			|| COMPLETED.equals(store.getStoreStatus())
    			|| groupOrderService.proceed(groupOrder) == 0 ) {
    		return Map.of("result",false);
    	}
    	sseService.statusChangeOrder(groupOrder.getOrderIdx(), ORDER_STATUSES.get(ORDER_STATUSES.indexOf(groupOrder.getOrderStatus())+1));
    	return Map.of("result",true);
    }

    // 가게 상태 수정
    @PostMapping("/owners/store/status")
    public String updateStatus(
    		RedirectAttributes ra,
    		@RequestParam("storeIdx") int storeIdx, 
    		@RequestParam("storeStatus") String storeStatus,
			@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if ( store==null ) {
    		ra.addFlashAttribute("error", "잘못된 접근입니다");
    		return "redirect:/home";
    	}
    	
        storeService.updateStatus(storeIdx, member.getMemberIdx(), storeStatus);
        return "redirect:"+ redirectUrl;
    }

    // 가게 정보 수정
    @PostMapping("/owners/store/edit")
    public String updateStore(
    		RedirectAttributes ra,
    		@ModelAttribute Store store,
			@SessionAttribute("member") Member member) {
    	Store check = storeService.myStore(member.getMemberIdx());
    	if ( check==null || check.getStoreIdx()!=store.getStoreIdx() ) {
    		ra.addFlashAttribute("error", "잘못된 접근입니다");
    		return "redirect:/home";
    	}
        store.setOwnerMemberIdx(member.getMemberIdx());
        storeService.updateStore(store);
        return "redirect:/owners/store";
    }

    // 가게 정보 수정 폼
    @GetMapping("/owners/store/edit")
    public String updateStore(
    		RedirectAttributes ra,
    		Model model,
			@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if ( store==null ) {
    		ra.addFlashAttribute("error", "가게가 없습니다.");
    		return "redirect:/owners/store/new";
    	}

        model.addAttribute("menu", "store");
        model.addAttribute("store", store);
        return "views/owner/store-edit";
    }

    // 가게 등록
    @PostMapping("/owners/store")
    public String insertStore(
    		@ModelAttribute Store store,
			@SessionAttribute("member") Member member) {
        store.setOwnerMemberIdx(member.getMemberIdx());
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
}