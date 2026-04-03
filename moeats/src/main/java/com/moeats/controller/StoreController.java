package com.moeats.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.moeats.domain.Store;
import com.moeats.dto.StoreSearchCond;
import com.moeats.service.StoreService;

@Controller
public class StoreController {
	
	@Autowired
	private StoreService storeService;
	
	// 가게 전체 조회
	@GetMapping("/stores")
	@ResponseBody
	public List<Store> storeList(StoreSearchCond cond, int memberIdx) {

	    return storeService.getStoreList(cond, memberIdx);
	}
	
	// 가게 상태 수정(수정 폼에서 관리 or 내 가게 정보에서 관리)
	// 활성화, 비활성화 / responseBody 사용?
	@PostMapping("/owners/store/status")
	public String updateStatus(@RequestParam("storeIdx") int storeIdx,
			@RequestParam("ownerMemberIdx") int ownerMemberIdx,
		    @RequestParam("storeStatus") String storeStatus) {
		
		storeService.updateStatus(storeIdx, ownerMemberIdx, storeStatus);
		
		return "redirect:/owners/store";
	}
	
	// 가게 정보 수정
	@PostMapping("/owners/store/edit")
	public String updateStore(Store store) {
		
		storeService.updateStore(store);
		
		return "redirect:/owners/store";
	}
	
	// 가게 정보 수정 폼
	@GetMapping("/owners/store/edit")
	public String updateStore() {
		
		
		
		return "views/owner/store-edit";
	}
	
	
	// 가게 등록
	@PostMapping("/owners/store")
	public String insertStore(Store store) {
		
		storeService.insertStore(store);
		
		return "redirect:/owners/store";
	}
	
	// 가게 등록 폼
	@GetMapping("/owners/store/new")
	public String insertStore() {
		
		return "views/owner/store-create";
	}
	
	// 내 가게 조회
	
	
	// 내 가게 정보 폼
	@GetMapping("/owners/store")
	public String myStore() {
		
		return "views/owner/store-manage";
	}
	
}
