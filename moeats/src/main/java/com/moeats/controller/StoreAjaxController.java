package com.moeats.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.moeats.domain.Member;
import com.moeats.domain.Store;
import com.moeats.dto.StoreSearchCond;
import com.moeats.service.StoreService;

@RestController
public class StoreAjaxController {

    @Autowired
    private StoreService storeService;

    /**
     * 메인페이지 전용 가게 목록 AJAX
     *
     * 호출 예시:
     * /ajax/stores
     * /ajax/stores?keyword=치킨
     * /ajax/stores?category=CHICKEN
     * /ajax/stores?keyword=피자&category=PIZZA
     */
    @GetMapping("/ajax/stores")
    public List<Store> storeListAjax(
            StoreSearchCond cond,
            @SessionAttribute(name = "member", required = false) Member member) {

        int memberIdx = 0;

        if (member != null) {
            memberIdx = member.getMemberIdx();
        }

        try {
            return storeService.getStoreList(cond, memberIdx);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}