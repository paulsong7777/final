package com.moeats.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.moeats.domain.MenuImage;
import com.moeats.service.MenuImageService;

@Controller
@RequestMapping("/owners")
public class MenuImageController {
	
	@Autowired
	private MenuImageService menuImageService;
	
	// 이미지 삭제
	@PostMapping("/menus/{menuIdx}/images/{menuImageIdx}/delete")
	@ResponseBody
	public String deleteImage(@PathVariable int menuIdx,
			@PathVariable int menuImageIdx) {
		
		menuImageService.deleteImage(menuImageIdx);
		// 실제 서버에 저장된 파일 지우는 로직 필요
		return "OK";
	}
	
	/**
	 * 이미지 대표사진 수정 (AJAX)
	 * 폼 이동 없이 화면에서 '별표(★)' 버튼 누르면 바로 작동하도록 구성
	 */
	@PostMapping("/menus/{menuIdx}/images/{menuImageIdx}/primary")
	@ResponseBody
	public String changePrimaryImage(@PathVariable("menuIdx") int menuIdx,
									 @PathVariable("menuImageIdx") int menuImageIdx) {
		
		menuImageService.changePrimaryImage(menuIdx, menuImageIdx);
		
		return "OK";
	}
	
	
	// 이미지 수정
	@PostMapping("/menus/{menuIdx}/images/{menuImageIdx}/edit")
	public String updateImage(@PathVariable int menuIdx,
				@PathVariable int menuImageIdx,
				@RequestParam("file") MultipartFile file) {
		
		// 기존 파일 삭제 + 새 파일 업로드 + DB업데이트
		
		return "redirect:/owners/menus/" + menuIdx + "/images";
	}
	// 이미지 수정 폼
	@GetMapping("/menus/{menuIdx}/images/{menuImageIdx}/edit")
	public String updateImageForm(@PathVariable("menuIdx") int menuIdx,
			@PathVariable("menuImageIdx") int menuImageIdx,
			Model model) {
		
		model.addAttribute("menuIdx", menuIdx);
		model.addAttribute("menuImageIdx", menuImageIdx);
		
		return "views/owner/menu-image-edit";
	}
	
	
	// 이미지 등록
	@PostMapping("/menus/{menuIdx}/images")
	public String insertImage(@PathVariable int menuIdx,
				@RequestParam("file") MultipartFile file) {
		
		// 파일업로드 로직 + menuImageService.insertImage()
		return "redirect:/owners/menus/" + menuIdx + "/images";
	}
	
	// 이미지 등록 폼
	@GetMapping("/menus/{menuIdx}/images/new")
	public String insertImageForm(@PathVariable("menuIdx") int menuIdx, Model model) {
		
		model.addAttribute("menuIdx", menuIdx);
		
		return "views/owner/menu-image-create";
	}

	/**
	 * 메뉴에 해당하는 이미지 전체 조회 (관리 갤러리 폼)
	 * 배민 사장님 광장처럼 한 메뉴의 사진을 쫙 펼쳐보는 화면입니다.
	 */
	@GetMapping("/menus/{menuIdx}/images")
	public String imageListByMenu(@PathVariable("menuIdx") int menuIdx, Model model) {
		
		List<MenuImage> imageList = menuImageService.imageByIdx(menuIdx);
		
		model.addAttribute("menuIdx", menuIdx);
		model.addAttribute("imageList", imageList);
		
		return "views/owner/menu-image-manage"; 
	}
	
	//	이미지 전체조회 (관리자용 등)
		@GetMapping("/all-image")
		public String imageList(Model model) {
			
			// 🟢 서비스 메서드 (모든 사진)
			List<MenuImage> allImages = menuImageService.imageList();
			model.addAttribute("allImages", allImages);
			
			return "views/owner/all-image-list"; 
		}
	
	
}
