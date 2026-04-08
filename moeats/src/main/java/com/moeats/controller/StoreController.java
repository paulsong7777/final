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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.Member;
import com.moeats.domain.Store;
import com.moeats.dto.StoreSearchCond;
import com.moeats.service.StoreService;
import com.moeats.services.GroupOrderService;
import com.moeats.services.GroupOrderService.GroupOrderRecord;

@Controller
public class StoreController {

    @Autowired
    private StoreService storeService;
    @Autowired
    private GroupOrderService groupOrderService;
    
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
	    	ra.addAttribute("error", "가게가 없습니다");
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
			ra.addAttribute("error", "가게가 없습니다");
			return "redirect:/owners/store/new";
		}
		List<GroupOrderRecord> groupOrders = groupOrderService.findRecordByStore(store.getStoreIdx());
		
		model.addAttribute("menu", "order");
		model.addAttribute("store", store);
		model.addAttribute("groupOrders", groupOrders);
		return "views/owner/order-list"; 
	}
	
    @GetMapping("/owners/order/detail")
    public String orderDetail(
    		RedirectAttributes ra,
    		Model model,
    		@RequestParam(name = "roomIdx", defaultValue = "0") Integer roomIdx,
			@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if ( store==null ) {
    		ra.addAttribute("error", "가게가 없습니다");
    		return "redirect:/owners/store/new";
    	}
    	if( roomIdx==0 ) {
    		ra.addAttribute("error","잘못된 주문 번호입니다");
    		return "redirect:/owners/dashboard";
    	}
    	GroupOrderRecord groupOrder = groupOrderService.findRecordByIdx(roomIdx);
    	if( groupOrder.groupOrder()==null
    			|| groupOrder.groupOrder().getStoreIdx() != store.getStoreIdx() ) {
    		ra.addAttribute("error","잘못된 주문 번호입니다");
    		return "redirect:/owners/dashboard";
    	}
    	
    	model.addAttribute("menu", "order");
    	model.addAttribute("store", store);
        model.addAttribute("groupOrder", groupOrder);
        return "views/owner/order-detail"; 
    }

    // 가게 상태 수정
    @PostMapping("/owners/store/status")
    public String updateStatus(@RequestParam("storeIdx") int storeIdx,
                               @RequestParam("storeStatus") String storeStatus,
                               @SessionAttribute("member") Member member) {

        storeService.updateStatus(storeIdx, member.getMemberIdx(), storeStatus);
        return "redirect:/owners/store";
    }

    // 가게 정보 수정
    @PostMapping("/owners/store/edit")
    public String updateStore(Store store,
                              @SessionAttribute("member") Member member) {
    	
        store.setOwnerMemberIdx(member.getMemberIdx());
        storeService.updateStore(store);
        return "redirect:/owners/store";
    }

    // 가게 정보 수정 폼
    @GetMapping("/owners/store/edit")
    public String updateStore(@SessionAttribute("member") Member member,
                              Model model) {

        Store store = storeService.myStore(member.getMemberIdx());
        model.addAttribute("store", store);

        return "views/owner/store-edit";
    }

    // 가게 등록
    @PostMapping("/owners/store")
    public String insertStore(Store store,
                              @SessionAttribute("member") Member member) {

        store.setOwnerMemberIdx(member.getMemberIdx());
        storeService.insertStore(store);

        return "redirect:/owners/store";
    }

    // 가게 등록 폼
    @GetMapping("/owners/store/new")
    public String insertStore() {
        return "views/owner/store-create";
    }
    
    // 내 가게 조회
    @GetMapping("/owners/store")
    public String myStore(@SessionAttribute("member") Member member,
    		Model model) {
    	
    	Store store = storeService.myStore(member.getMemberIdx());
    	model.addAttribute("store", store);
    	
    	return "views/owner/store-manage";
    }
}