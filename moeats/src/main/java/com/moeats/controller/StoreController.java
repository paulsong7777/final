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

import com.moeats.domain.Member;
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

    @GetMapping("/owners/order-list")
    public String orderList() {
        return "views/owner/order-list"; 
    }

    /**
     * ✅ [신규 추가]: 기존 회원 전용 지도 대시보드 페이지 연동
     * [추가 사유]: ViewOwnerController의 대시보드가 주석 처리되어 있으므로, 
     * DB에서 실시간 매장 데이터(storeVo)를 조회하여 지도를 띄우기 위한 별도의 매핑 생성.
     * 접속 주소: /owners/dashboard-map
     */
    @GetMapping("/owners/dashboard-map")
    public String dashboardMap(@SessionAttribute("member") Member member, Model model) {
        // 1. 세션의 회원 번호로 DB에서 매장 정보 조회
        Store store = storeService.myStore(member.getMemberIdx());
        
        // 2. 매장 정보가 존재할 때만 storeVo를 모델에 담아 지도 화면 활성화
        if (store != null) {
            model.addAttribute("storeVo", store);
            model.addAttribute("menu", "dash"); // 사이드바 활성화 태그
        }
        
        // 3. 지도가 포함된 대시보드 뷰 호출
        return "views/owner/dashboard";
    }
}