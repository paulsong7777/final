package com.moeats.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import com.moeats.service.MenuImageService;
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
    private MenuImageService menuImageService;
    @Autowired
    private StoreMenuCategoryService storeMenuCategoryService;

    // 파일이 저장될 기본 폴더 경로 (C드라이브에 이 폴더를 꼭 만들어주세요!)
    private final String UPLOAD_DIR = "C:/moeats_uploads/";
    
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
	 * * List<StoreMenu> menuList = storeMenuService.menuListForUser(storeIdx);
	 * * model.addAttribute("menuList", menuList);
	 * * return "views/user/menu-list"; }
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
	 * * @PathVariable("storeIdx") int storeIdx,
	 * * @RequestParam String keyword, Model model) {
	 * * List<StoreMenu> menuList = storeMenuService.searchMenuForUser(storeIdx,
	 * keyword);
	 * * model.addAttribute("menuList", menuList); model.addAttribute("keyword",
	 * keyword);
	 * * return "views/user/menu-list"; }
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
        
        Store store = storeService.myStore(member.getMemberIdx());
        if (store == null) return Map.of("result", false, "message", "가게 정보가 없습니다.");

        try {
            // 🚨 수정: deleteImage가 아니라 deleteImagesByMenuIdx를 호출해야 합니다!
            menuImageService.deleteImagesByMenuIdx(menuIdx); 
        } catch (Exception e) {
            e.printStackTrace();
        }

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
    
 // ✨ 메뉴 등록 (파일 업로드 처리 추가 및 순서 변경)
    @PostMapping("/owners/menu")
    @ResponseBody
    public Map insertMenu(
            @ModelAttribute StoreMenu storeMenu,
            @RequestParam(value="menuFile", required=false) MultipartFile menuFile,
            @SessionAttribute("member") Member member) {
        
        Store store = storeService.myStore(member.getMemberIdx());
        StoreMenuCategory storeMenuCategory = storeMenuCategoryService.getCategory(storeMenu.getMenuCategoryIdx());
        
        if (store == null || storeMenuCategory == null || storeMenuCategory.getStoreIdx() != store.getStoreIdx()) {
            return Map.of("result", false);
        }
        storeMenu.setStoreIdx(store.getStoreIdx());

        // 1. 메뉴 정보를 DB에 먼저 저장
        storeMenuService.insertMenu(storeMenu);

        // 2. 파일이 업로드된 경우, menu_image 테이블에 사진 정보를 따로 저장
        if (menuFile != null && !menuFile.isEmpty()) {
            String originalName = menuFile.getOriginalFilename();
            String savedName = java.util.UUID.randomUUID().toString() + "_" + originalName; 
            
            try {
                // 실제 파일 복사
                java.io.File dest = new java.io.File(UPLOAD_DIR + savedName);
                menuFile.transferTo(dest);
                
                // 별도의 MenuImage 객체를 만들어서 DB에 INSERT
                com.moeats.domain.MenuImage menuImage = new com.moeats.domain.MenuImage();
                menuImage.setMenuIdx(storeMenu.getMenuIdx());     
                menuImage.setImageUrl("/uploads/" + savedName);   
                
                // 🚨 오류나던 부분 수정 + 기획 반영: 
                // 대표사진 여부를 우선 false로 둡니다. (만약 빨간줄 나면 setIsPrimary(false) 또는 setIsPrimary("N")으로 변경)
                menuImage.setPrimary(true); 
                
                menuImage.setDisplayOrder(1);                     
                
                menuImageService.insertImage(menuImage); 
                
            } catch (Exception e) {
                e.printStackTrace();
                return Map.of("result", false, "message", "파일 업로드 중 오류가 발생했습니다.");
            }
        }
        
        return Map.of("result", true);
    }
    
 // 메뉴 수정 폼
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
            return "redirect:/owners/menu"; // 🚨 잘못된 접근 시 메뉴 목록으로 돌려보냅니다.
        }

        List<StoreMenuCategory> storeMenuCategories = storeMenuCategoryService.getCategoryByStore(store.getStoreIdx());

        model.addAttribute("menu", "menu-edit");
        model.addAttribute("store", store);
        model.addAttribute("storeMenu", storeMenu);
        model.addAttribute("storeMenuCategories", storeMenuCategories);

        return "views/owner/menu-edit";
    }
    
 // ✨ 메뉴 수정 (새 사진 업로드 시 DB 누락 문제 해결)
    @PostMapping("/owners/menu/edit")
    public String updateMenu(
            RedirectAttributes ra,
            @ModelAttribute StoreMenu storeMenu,
            @RequestParam(value="menuFile", required=false) MultipartFile menuFile, 
            @SessionAttribute("member") Member member){
        
        Store store = storeService.myStore(member.getMemberIdx());
        if (store == null) {
            ra.addFlashAttribute("error", "가게가 없습니다.");
            return "redirect:/owners/store/new"; 
        }

        storeMenu.setStoreIdx(store.getStoreIdx());

        StoreMenu check = storeMenuService.getMenu(store.getStoreIdx(), storeMenu.getMenuIdx());
        if (check == null || check.getStoreIdx() != store.getStoreIdx()) {
            ra.addFlashAttribute("error", "잘못된 접근입니다");
            return "redirect:/owners/menu"; 
        }

     // 🚨 메뉴 수정 로직 수행 (StoreMenu에는 텍스트 정보만 들어감)
        storeMenuService.updateMenu(storeMenu);

        // 사진이 새로 업로드 된 경우에만 MenuImage 갤러리에 추가해줍니다.
        if (menuFile != null && !menuFile.isEmpty()) {
            String originalName = menuFile.getOriginalFilename();
            String savedName = UUID.randomUUID().toString() + "_" + originalName; 
            
            try {
                java.io.File dest = new java.io.File(UPLOAD_DIR + savedName);
                menuFile.transferTo(dest);
                
                // 🚨 [핵심 해결] 새 사진을 대표로 넣기 전에, 이 메뉴의 기존 사진들의 '대표' 자격을 모두 뺏어옵니다!
                menuImageService.clearPrimaryImage(storeMenu.getMenuIdx());
                
                com.moeats.domain.MenuImage menuImage = new com.moeats.domain.MenuImage();
                menuImage.setMenuIdx(storeMenu.getMenuIdx());
                menuImage.setImageUrl("/uploads/" + savedName);
                
                // 이제 이 새 사진만 유일한 '대표 사진'이 됩니다.
                menuImage.setPrimary(true); 
                menuImage.setDisplayOrder(1);
                
                menuImageService.insertImage(menuImage);
                
            } catch (Exception e) {
                e.printStackTrace();
                ra.addFlashAttribute("error", "파일 업로드 실패");
                return "redirect:/owners/menu/edit?menuIdx=" + storeMenu.getMenuIdx();
            }
        }

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