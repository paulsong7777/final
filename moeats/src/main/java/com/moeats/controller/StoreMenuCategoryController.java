package com.moeats.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.Member;
import com.moeats.domain.Store;
import com.moeats.domain.StoreMenuCategory;
import com.moeats.service.StoreMenuCategoryService;
import com.moeats.service.StoreService;

@Controller
@RequestMapping("/owners/category")
public class StoreMenuCategoryController {
	@Autowired
	private StoreMenuCategoryService storeMenuCategoryService;
	@Autowired
	private StoreService storeService;
	
	// 1. 목록
	@GetMapping
	public String list(
			RedirectAttributes ra,
			Model model,
			@SessionAttribute("member") Member member) {
		Store store = storeService.myStore(member.getMemberIdx());
		if (store == null) {
			ra.addFlashAttribute("error","가게가 없습니다");
			return "redirect:/owners/store/new";
		}
		List<StoreMenuCategory> storeMenuCategories = storeMenuCategoryService.getCategoryByStore(store.getStoreIdx());
		
        model.addAttribute("menu", "category");
		model.addAttribute("store", store);
		model.addAttribute("storeMenuCategories", storeMenuCategories);
		return "category/list";
	}

	// 2. 등록 폼
	@GetMapping("/write")
	public String writeForm(
			RedirectAttributes ra,
			Model model,
			@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if ( store==null ) {
    		ra.addAttribute("error", "가게가 없습니다");
    		return "redirect:/owners/store/new";
    	}
    	
    	model.addAttribute("menu", "category-reg");
    	model.addAttribute("store", store);
		return "category/write";
	}

	// 3. 등록
	@PostMapping("/write")
	public String create(
			RedirectAttributes ra,
			@ModelAttribute StoreMenuCategory category,
			@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if ( store==null ) {
    		ra.addAttribute("error", "잘못된 접근입니다");
    		return "redirect:/home";
    	}
		category.setStoreIdx(store.getStoreIdx());
		
		storeMenuCategoryService.createCategory(category);
		return "redirect:/owners/store-menu-category";
	}

	// 4. 상세
	@GetMapping("/{menuCategoryIdx}")
	public String detail(
			RedirectAttributes ra,
			Model model,
			@PathVariable int menuCategoryIdx,
			@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if ( store==null ) {
    		ra.addAttribute("error", "가게가 없습니다");
    		return "redirect:/owners/store/new";
    	}

		StoreMenuCategory category = storeMenuCategoryService.getCategory(menuCategoryIdx);
		
		model.addAttribute("category", category);
		return "category/detail";
	}

	// 5. 수정 폼
	@GetMapping("/{menuCategoryIdx}/edit")
	public String editForm(
			RedirectAttributes ra,
			Model model,
			@PathVariable int menuCategoryIdx,
			@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if ( store==null ) {
    		ra.addAttribute("error", "가게가 없습니다");
    		return "redirect:/owners/store/new";
    	}
    	
		StoreMenuCategory category = storeMenuCategoryService.getCategory(menuCategoryIdx);
		
		model.addAttribute("category", category);
		
		return "category/edit";
	}

	// 6. 수정
	@PostMapping("/{menuCategoryIdx}/edit")
	public String update(
			RedirectAttributes ra,
			@PathVariable int menuCategoryIdx,
			@ModelAttribute StoreMenuCategory category,
			@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	StoreMenuCategory check = storeMenuCategoryService.getCategory(menuCategoryIdx);
    	if ( store==null || check==null || check.getStoreIdx()!=store.getStoreIdx() ) {
    		ra.addAttribute("error", "잘못된 접근입니다");
    		return "redirect:/home";
    	}
    	
		category.setMenuCategoryIdx(menuCategoryIdx);
		category.setStoreIdx(store.getStoreIdx());
		
		storeMenuCategoryService.updateCategory(category);

		return "redirect:/owners/store-menu-category";
	}

	// 7. 삭제
	@PostMapping("/{menuCategoryIdx}/delete")
	public String delete(
			RedirectAttributes ra,
			@PathVariable int menuCategoryIdx,
			@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	StoreMenuCategory check = storeMenuCategoryService.getCategory(menuCategoryIdx);
    	if ( store==null || check==null || check.getStoreIdx()!=store.getStoreIdx() ) {
    		ra.addAttribute("error", "잘못된 접근입니다");
    		return "redirect:/home";
    	}
    	
		storeMenuCategoryService.deleteCategory(menuCategoryIdx, store.getStoreIdx());
		
		return "redirect:/owners/store-menu-category";
	}
}