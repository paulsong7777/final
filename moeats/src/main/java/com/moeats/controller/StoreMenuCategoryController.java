package com.moeats.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

import com.moeats.domain.Member;
import com.moeats.domain.Store;
import com.moeats.domain.StoreMenuCategory;
import com.moeats.service.StoreMenuCategoryService;
import com.moeats.service.StoreService;

@Controller
@RequestMapping("/owners/store-menu-category")
public class StoreMenuCategoryController {
    
    @Autowired
    private StoreMenuCategoryService storeMenuCategoryService;

    @Autowired
    private StoreService storeService;

    // 🔥 공통: storeIdx 가져오기 (기존 유지)
    private int getStoreIdx(Member member) {
        Store store = storeService.myStore(member.getMemberIdx());
        if (store == null) {
            throw new IllegalStateException("가게가 등록되지 않았습니다.");
        }
        return store.getStoreIdx();
    }

    // 1. 목록 (기존 유지)
    @GetMapping
    public String list() {
        return "category/list";
    }

    // 2. 등록 폼 (기존 유지)
    @GetMapping("/write")
    public String writeForm() {
        return "category/write";
    }

    /**
     * 3. 등록 처리
     * [수정 내용]: AJAX 요청에 응답하기 위해 @ResponseBody 추가 및 리턴 타입을 ResponseEntity로 변경
     */
    @PostMapping("/write")
    @ResponseBody // 비동기 응답을 위해 추가
    public ResponseEntity<Map<String, Object>> create(StoreMenuCategory category,
                         @SessionAttribute("member") Member member) {
        Map<String, Object> result = new HashMap<>();
//        System.out.println(category);
//            int storeIdx = getStoreIdx(member);
//            category.setStoreIdx(storeIdx);
//            storeMenuCategoryService.createCategory(category);
//            
            result.put("success", true);
            return ResponseEntity.ok(result); // 성공 응답 전송
    }

    // 4. 상세 (기존 유지)
    @GetMapping("/{menuCategoryIdx}")
    public String detail(@PathVariable int menuCategoryIdx,
                         Model model,
                         @SessionAttribute("member") Member member) {
        int storeIdx = getStoreIdx(member);
        StoreMenuCategory category = storeMenuCategoryService.getCategory(menuCategoryIdx, storeIdx);
        model.addAttribute("category", category);
        return "category/detail";
    }

    // 5. 수정 폼 (기존 유지)
    @GetMapping("/{menuCategoryIdx}/edit")
    public String editForm(@PathVariable int menuCategoryIdx,
                           Model model,
                           @SessionAttribute("member") Member member) {
        int storeIdx = getStoreIdx(member);
        StoreMenuCategory category = storeMenuCategoryService.getCategory(menuCategoryIdx, storeIdx);
        model.addAttribute("category", category);
        return "category/edit";
    }

    /**
     * 6. 수정 처리
     * [수정 내용]: Fetch API 대응을 위해 @ResponseBody 및 비동기 결과 리턴 적용
     */
    @PostMapping("/{menuCategoryIdx}/edit")
    @ResponseBody // 비동기 응답 추가
    public ResponseEntity<Map<String, Object>> update(@PathVariable int menuCategoryIdx,
                         StoreMenuCategory category,
                         @SessionAttribute("member") Member member) {
        Map<String, Object> result = new HashMap<>();
        try {
            int storeIdx = getStoreIdx(member);
            category.setMenuCategoryIdx(menuCategoryIdx);
            category.setStoreIdx(storeIdx);
            storeMenuCategoryService.updateCategory(category);
            
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 7. 삭제 처리
     * [수정 내용]: 비동기 삭제 후 목록으로 자연스럽게 넘어가기 위해 리턴 타입 변경
     */
    @PostMapping("/{menuCategoryIdx}/delete")
    @ResponseBody // 비동기 응답 추가
    public ResponseEntity<Map<String, Object>> delete(@PathVariable int menuCategoryIdx,
                         @SessionAttribute("member") Member member) {
        Map<String, Object> result = new HashMap<>();
        try {
            int storeIdx = getStoreIdx(member);
            storeMenuCategoryService.deleteCategory(menuCategoryIdx, storeIdx);
            
            result.put("success", true);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            result.put("success", false);
            return ResponseEntity.status(500).body(result);
        }
    }
}