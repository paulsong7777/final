package com.moeats.controller;

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
@RequestMapping("/owners/store-menu-category")
public class StoreMenuCategoryController {
	
	
	@Autowired
	private StoreMenuCategoryService storeMenuCategoryService;

	@Autowired
	private StoreService storeService;

	// 🔥 공통: storeIdx 가져오기
	private int getStoreIdx(Member member) {
		Store store = storeService.myStore(member.getMemberIdx());

		if (store == null) {
			throw new IllegalStateException("가게가 등록되지 않았습니다.");
		}

		return store.getStoreIdx();
	}

	// 1. 목록
	@GetMapping
	public String list() {
		return "category/list";
	}

	// 2. 등록 폼
	@GetMapping("/write")
	public String writeForm(
			RedirectAttributes ra,
			@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if ( store==null ) {
    		ra.addAttribute("error", "가게가 없습니다");
    		return "redirect:/owners/store/new";
    	}
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
    		ra.addAttribute("error", "가게가 없습니다");
    		return "redirect:/owners/store/new";
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

		StoreMenuCategory category =
				storeMenuCategoryService.getCategory(menuCategoryIdx, store.getStoreIdx());

		model.addAttribute("category", category);

		return "category/detail";
	}

	// 5. 수정 폼
	@GetMapping("/{menuCategoryIdx}/edit")
	public String editForm(@PathVariable int menuCategoryIdx,
						   Model model,
						   @SessionAttribute("member") Member member) {

		int storeIdx = getStoreIdx(member);

		StoreMenuCategory category =
				storeMenuCategoryService.getCategory(menuCategoryIdx, storeIdx);

		model.addAttribute("category", category);

		return "category/edit";
	}

	// 6. 수정
	@PostMapping("/{menuCategoryIdx}/edit")
	public String update(@PathVariable int menuCategoryIdx,
						 StoreMenuCategory category,
						 @SessionAttribute("member") Member member) {

		int storeIdx = getStoreIdx(member);

		category.setMenuCategoryIdx(menuCategoryIdx);
		category.setStoreIdx(storeIdx);

		storeMenuCategoryService.updateCategory(category);

		return "redirect:/owners/store-menu-category";
	}

	// 7. 삭제
	@PostMapping("/{menuCategoryIdx}/delete")
	public String delete(@PathVariable int menuCategoryIdx,
						 @SessionAttribute("member") Member member) {

		int storeIdx = getStoreIdx(member);

		storeMenuCategoryService.deleteCategory(menuCategoryIdx, storeIdx);

		return "redirect:/owners/store-menu-category";
	}
}