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
}