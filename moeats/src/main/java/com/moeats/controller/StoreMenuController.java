package com.moeats.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.Member;
import com.moeats.domain.Store;
import com.moeats.domain.StoreMenu;
import com.moeats.service.StoreMenuService;
import com.moeats.service.StoreService;

import jakarta.servlet.http.HttpSession;

@Controller
public class StoreMenuController {

    @Autowired
    private StoreMenuService storeMenuService;
    @Autowired
    private StoreService storeService;


    /**
     * =========================
     * 👤 사용자용 (고객) - 비회원, 고객 전부 사용
     * =========================
     */

    // 메뉴 리스트 조회
    @GetMapping("/stores/{storeIdx}/menu")
    public String menuListForUser(@PathVariable int storeIdx, Model model) {

        List<StoreMenu> menuList = storeMenuService.menuListForUser(storeIdx);

        model.addAttribute("menuList", menuList);

        return "views/user/menu-list";
    }


    // 메뉴 검색
    @GetMapping("/stores/{storeIdx}/menu/search")
    public String searchMenuForUser(
            Model model,
            @PathVariable int storeIdx,
            @RequestParam String keyword) {

        List<StoreMenu> menuList = storeMenuService.searchMenuForUser(storeIdx, keyword);

        model.addAttribute("menuList", menuList);
        model.addAttribute("keyword", keyword);

        return "views/user/menu-list";
    }


    /**
     * =========================
     * 🧑‍🍳 점주용 (관리자)
     * =========================
     */

    // 메뉴 관리 화면
    @GetMapping("/owners/menu")
    public String menuListForOwner(
    		RedirectAttributes ra,
    		Model model,
    		@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if(store==null) {
    		ra.addFlashAttribute("error", "가게가 없습니다");
    		return "redirect:/home";
    	}

        List<StoreMenu> menuList = storeMenuService.menuList(store.getStoreIdx());

        model.addAttribute("menuList", menuList);

        return "views/owner/menu-manage";
    }


    // 메뉴 등록
    @PostMapping("/owners/menu")
    public String insertMenu(
    		RedirectAttributes ra,
    		@ModelAttribute StoreMenu storeMenu,
    		@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if(store==null) {
    		ra.addFlashAttribute("error", "가게가 없습니다");
    		return "redirect:/home";
    	}
    	
        storeMenu.setStoreIdx(store.getStoreIdx()); // 강제 세팅 (보안)
        storeMenuService.insertMenu(storeMenu);

        return "redirect:/owners/menu";
    }


    // 메뉴 수정
    @PostMapping("/owners/menu/edit")
    public String updateMenu(
    		RedirectAttributes ra,
    		@ModelAttribute StoreMenu storeMenu,
    		@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if(store==null) {
    		ra.addFlashAttribute("error", "잘못된 접근입니다");
    		return "redirect:/home";
    	}
    	
    	StoreMenu pre = storeMenuService.getMenu(store.getStoreIdx(),storeMenu.getMenuIdx());
		if(pre.getStoreIdx()!=store.getStoreIdx()) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return "redirect:/home";
		}
        storeMenuService.updateMenu(storeMenu);

        return "redirect:/owners/menu";
    }


    // 메뉴 상태 변경 (AJAX 추천)
    @PostMapping("/owners/menu/status")
    @ResponseBody
    public String updateStatus(
            @RequestParam int menuIdx,
            @RequestParam String menuStatus,
            @SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if(store==null) {
    		return "Fail";
    	}

        storeMenuService.updateStatus(store.getStoreIdx(), menuIdx, menuStatus);

        return "OK";
    }

}