package com.moeats.controller;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.Member;
import com.moeats.domain.Store;
import com.moeats.domain.StoreMenu;
import com.moeats.domain.StoreMenuCategory;
import com.moeats.service.StoreMenuCategoryService;
import com.moeats.service.StoreMenuService;
import com.moeats.service.StoreService;

@Controller
public class StoreMenuController {
	@Autowired
	private StoreService storeService;
    @Autowired
    private StoreMenuService storeMenuService;
    @Autowired
    private StoreMenuCategoryService storeMenuCategoryService;

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
    public String menuListForOwner(
    		RedirectAttributes ra,
    		Model model,
			@SessionAttribute("member") Member member) {
		Store store = storeService.myStore(member.getMemberIdx());
	    if ( store==null ) {
	    	ra.addAttribute("error", "가게가 없습니다");
	    	return "redirect:/owners/store/new";
	    }
	    
        List<StoreMenu> menuList = storeMenuService.menuList(store.getStoreIdx());
        
        model.addAttribute("menu", "menu-mgmt");
        model.addAttribute("store", store);
        model.addAttribute("menuList", menuList);
        
        return "views/owner/menu-manage";
    }
    
    @PostMapping("/owners/menu/delete")
    @ResponseBody
    public Map<String, Object> deleteMenu(
            @RequestParam("menuIdx") int menuIdx, 
            @SessionAttribute("member") Member member) {
        
        // 1. 현재 로그인한 점주의 가게 정보 확인
        Store store = storeService.myStore(member.getMemberIdx());
        if (store == null) {
            return Map.of("result", false, "message", "가게 정보가 없습니다.");
        }

        // 2. 보안 체크: 삭제하려는 메뉴가 진짜 이 사장님 가게 메뉴인지 확인 후 삭제
        // (이 로직은 서비스에서 처리하는 것이 안전합니다)
        boolean isDeleted = storeMenuService.deleteMenu(store.getStoreIdx(), menuIdx);
        
        return Map.of("result", isDeleted);
    }


    // 메뉴 등록
    @GetMapping("/owners/menu/new")
    public String newMenuForm(
    		RedirectAttributes ra,
    		Model model,
    		@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if ( store==null ) {
    		ra.addAttribute("error", "가게가 없습니다");
    		return "redirect:/owners/store/new";
    	}
    	List<StoreMenuCategory> storeMenuCategories = storeMenuCategoryService.getCategoryByStore(store.getStoreIdx());
    	model.addAttribute("menu", "menu-reg");
    	model.addAttribute("store", store);
    	model.addAttribute("storeMenuCategories", storeMenuCategories);
    	return "views/owner/menu-register";
    }
    
    @PostMapping("/owners/menu")
    @ResponseBody
    public Map insertMenu(
    		@ModelAttribute StoreMenu storeMenu,
    		@RequestParam(value="menuFile", required=false) MultipartFile menuFile,
			@SessionAttribute("member") Member member) {
		Store store = storeService.myStore(member.getMemberIdx());
		StoreMenuCategory storeMenuCategory = storeMenuCategoryService.getCategory(storeMenu.getMenuCategoryIdx());
	    if ( store==null || storeMenuCategory==null || storeMenuCategory.getStoreIdx()!=store.getStoreIdx() ) {
	    	return Map.of("result",false);
	    }
	    storeMenu.setStoreIdx(store.getStoreIdx()); // 강제 세팅
	    
	    // [임시 테스트용] 실제 파일 저장 대신 인터넷 이미지 주소를 강제로 넣음
	    // -----------------------------------------------------------
	 // 🚨 조건문 밖으로 뺐습니다. 무조건 값이 들어가야 함!
	    storeMenu.setImageUrl("https://placehold.jp/24/ff8000/ffffff/200x200.png?text=TEST_SAVE");
	    
	    System.out.println("저장하려는 이미지 경로: " + storeMenu.getImageUrl()); // 콘솔 출력 확인용
	    
	    // TODO 파일 받아서 저장하기
        storeMenuService.insertMenu(storeMenu);
        return Map.of("result",true);
    }
    
    // 메뉴 수정
    @GetMapping("/owners/menu/edit")
    public String updateMenuForm(
            RedirectAttributes ra,
            Model model,
            @RequestParam(name = "menuIdx", defaultValue = "0") int menuIdx,
            @SessionAttribute("member") Member member) {

        Store store = storeService.myStore(member.getMemberIdx());
        if (store == null) {
            ra.addFlashAttribute("error", "가게가 없습니다");
            return "redirect:/owners/store/new";
        }

        StoreMenu storeMenu = storeMenuService.getMenu(store.getStoreIdx(), menuIdx);
        if (storeMenu == null || storeMenu.getStoreIdx() != store.getStoreIdx()) {
            ra.addFlashAttribute("error", "잘못된 접근입니다");
            return "redirect:/owners/menu";
        }

        List<StoreMenuCategory> storeMenuCategories =
                storeMenuCategoryService.getCategoryByStore(store.getStoreIdx());

        model.addAttribute("menu", "menu-edit");
        model.addAttribute("store", store);
        model.addAttribute("storeMenu", storeMenu);
        model.addAttribute("storeMenuCategories", storeMenuCategories);

        return "views/owner/menu-edit";
    }
    
    @PostMapping("/owners/menu/edit")
    public String updateMenu(
    		RedirectAttributes ra,
    		@ModelAttribute StoreMenu storeMenu,
			@SessionAttribute("member") Member member) {
		Store store = storeService.myStore(member.getMemberIdx());
	    if ( store==null ) {
	    	ra.addAttribute("error", "잘못된 접근입니다");
	    	return "redirect:/home";
	    }

        StoreMenu check = storeMenuService.getMenu(store.getStoreIdx(), storeMenu.getMenuIdx());
        if ( check==null || check.getStoreIdx()!=store.getStoreIdx() ) {
        	ra.addAttribute("error", "잘못된 접근입니다");
        	return "redirect:/home";
        }

        storeMenuService.updateMenu(storeMenu);

        return "redirect:/owners/menu";
    }
    
    // 메뉴 상태 변경 (AJAX 추천)
    @PostMapping("/owners/menu/status")
    @ResponseBody
    public Map updateStatus(
    		@RequestParam(name = "menuIdx") int menuIdx, 
    	    @RequestParam(name = "menuStatus") String menuStatus, 
    	    @SessionAttribute(name = "member") Member member) {
		Store store = storeService.myStore(member.getMemberIdx());
	    if ( store==null ) {
	    	return Map.of("result",false);
	    }
	    StoreMenu check = storeMenuService.getMenu(store.getStoreIdx(), menuIdx);
	    if ( check==null || check.getStoreIdx()!=store.getStoreIdx() ) {
	    	return Map.of("result",false);
	    }

        storeMenuService.updateStatus(store.getStoreIdx(), menuIdx, menuStatus);
        return Map.of("result",true);
    }

}