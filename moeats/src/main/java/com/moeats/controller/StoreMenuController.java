package com.moeats.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.moeats.domain.Store;
import com.moeats.service.StoreService;
import com.moeats.domain.StoreMenu;
import com.moeats.service.StoreMenuService;

import jakarta.servlet.http.HttpSession;

@Controller
public class StoreMenuController {

	
	@Autowired
	private StoreService storeService;
    @Autowired
    private StoreMenuService storeMenuService;


    /**
     * =========================
     * 👤 사용자용 (고객) - 비회원, 고객 전부 사용
     * =========================
     */

    // 메뉴 리스트 조회
    // 주문방 생성 위해서 수정함. -영훈
    @GetMapping("/stores/{storeIdx}/menu")
    public String menuListForUser(@PathVariable("storeIdx") int storeIdx, Model model) {

        List<StoreMenu> menuList = storeMenuService.menuListForUser(storeIdx);
        Store store = storeService.getStoreByIdx(storeIdx);

        model.addAttribute("menuList", menuList);
        model.addAttribute("store", store);

        return "views/user/menu-list";
    }
    
	/*
	 * @GetMapping("/stores/{storeIdx}/menu") public String
	 * menuListForUser(@PathVariable("storeIdx") int storeIdx, Model model) {
	 * 
	 * List<StoreMenu> menuList = storeMenuService.menuListForUser(storeIdx);
	 * 
	 * model.addAttribute("menuList", menuList);
	 * 
	 * return "views/user/menu-list"; }
	 */


    // 메뉴 검색
    // store 모델에 추가. 영훈
    @GetMapping("/stores/{storeIdx}/menu/search")
    public String searchMenuForUser(
            @PathVariable("storeIdx") int storeIdx,
            @RequestParam String keyword,
            Model model) {

        List<StoreMenu> menuList = storeMenuService.searchMenuForUser(storeIdx, keyword);
        Store store = storeService.getStoreByIdx(storeIdx);

        model.addAttribute("menuList", menuList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("store", store);

        return "views/user/menu-list";
    }
    
	/*
	 * @GetMapping("/stores/{storeIdx}/menu/search") public String
	 * searchMenuForUser(
	 * 
	 * @PathVariable("storeIdx") int storeIdx,
	 * 
	 * @RequestParam String keyword, Model model) {
	 * 
	 * List<StoreMenu> menuList = storeMenuService.searchMenuForUser(storeIdx,
	 * keyword);
	 * 
	 * model.addAttribute("menuList", menuList); model.addAttribute("keyword",
	 * keyword);
	 * 
	 * return "views/user/menu-list"; }
	 */


    /**
     * =========================
     * 🧑‍🍳 점주용 (관리자)
     * =========================
     */

    // 메뉴 관리 화면
    @GetMapping("/owners/menu")
    public String menuListForOwner(HttpSession session, Model model) {

        int storeIdx = (int) session.getAttribute("storeIdx"); // 로그인 기반

        List<StoreMenu> menuList = storeMenuService.menuList(storeIdx);

        model.addAttribute("menuList", menuList);

        return "views/owner/menu-manage";
    }


    // 메뉴 등록
    @PostMapping("/owners/menu")
    public String insertMenu(StoreMenu storeMenu, HttpSession session) {

        int storeIdx = (int) session.getAttribute("storeIdx");

        storeMenu.setStoreIdx(storeIdx); // 강제 세팅 (보안)

        storeMenuService.insertMenu(storeMenu);

        return "redirect:/owners/menu";
    }


    // 메뉴 수정
    @PostMapping("/owners/menu/edit")
    public String updateMenu(StoreMenu storeMenu, HttpSession session) {

        int storeIdx = (int) session.getAttribute("storeIdx");

        storeMenu.setStoreIdx(storeIdx); // 강제 세팅

        storeMenuService.updateMenu(storeMenu);

        return "redirect:/owners/menu";
    }


    // 메뉴 상태 변경 (AJAX 추천)
    @PostMapping("/owners/menu/status")
    @ResponseBody
    public String updateStatus(
            @RequestParam int menuIdx,
            @RequestParam String menuStatus,
            HttpSession session) {

        int storeIdx = (int) session.getAttribute("storeIdx");

        storeMenuService.updateStatus(storeIdx, menuIdx, menuStatus);

        return "OK";
    }

}