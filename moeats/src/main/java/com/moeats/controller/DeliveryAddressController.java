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

import com.moeats.domain.DeliveryAddress;
import com.moeats.service.DeliveryAddressService;

import jakarta.servlet.http.HttpSession;

@Controller
public class DeliveryAddressController {
	
	@Autowired
	private DeliveryAddressService deliveryAddressService;
	

	
	// 삭제
	// 기본 배송지 삭제 막기 위해서 갈아엎는..다....아..아...ㅅㅂ..
	/*
	 * @PostMapping("/members/me/addresses/{deliveryAddressIdx}/delete") public
	 * String deleteAddress( @PathVariable("deliveryAddressIdx") int
	 * deliveryAddressIdx,
	 * 
	 * @SessionAttribute("memberIdx") int memberIdx) {
	 * 
	 * deliveryAddressService.deleteAddress(memberIdx, deliveryAddressIdx);
	 * 
	 * return "redirect:/members/me/addresses"; }
	 */
	
	@PostMapping("/members/me/addresses/{deliveryAddressIdx}/delete")
	public String deleteAddress(@PathVariable("deliveryAddressIdx") int deliveryAddressIdx,
	        @SessionAttribute("memberIdx") int memberIdx,
	        RedirectAttributes ra) {

	    try {
	        deliveryAddressService.deleteAddress(memberIdx, deliveryAddressIdx);
	        ra.addFlashAttribute("message", "배송지가 삭제되었습니다.");
	    } catch (IllegalStateException e) {
	        ra.addFlashAttribute("error", e.getMessage());
	    } catch (RuntimeException e) {
	        ra.addFlashAttribute("error", "배송지 삭제 중 문제가 발생했습니다.");
	    }

	    return "redirect:/members/me/addresses";
	}
	
	
	
	
	// 수정
	@PostMapping("/members/me/addresses/{deliveryAddressIdx}/edit")
	public String updateAddress(
	        @ModelAttribute DeliveryAddress deliveryAddress,
	        @SessionAttribute("memberIdx") int memberIdx) {

	    deliveryAddress.setMemberIdx(memberIdx);

	    deliveryAddressService.updateAddress(deliveryAddress);

	    return "redirect:/members/me/addresses";
	}
	
	// 주소 수정 폼 띄우기
	@GetMapping("/members/me/addresses/{deliveryAddressIdx}/edit")
	public String updateAddressForm(
	        @PathVariable("deliveryAddressIdx") int deliveryAddressIdx,
	        @SessionAttribute("memberIdx") int memberIdx,
	        Model model) {

	    DeliveryAddress address =
	            deliveryAddressService.addressByIdx(memberIdx, deliveryAddressIdx);

	    model.addAttribute("address", address);

	    return "views/members/address-edit";
	}
	
	
	// 등록
	@PostMapping("/members/me/addresses")
	public String insertAddress(@ModelAttribute DeliveryAddress deliveryAddress,
	        @SessionAttribute("memberIdx") int memberIdx,
	        RedirectAttributes ra) { // ✨ 에러 메시지 전달을 위해 추가
		
	    deliveryAddress.setMemberIdx(memberIdx);

	    try {
	        deliveryAddressService.insertAddress(deliveryAddress);
	    } catch (RuntimeException e) {
	        // ✨ 좌표 변환 실패 시 500 에러 대신 사용자에게 메시지 전달
	        ra.addFlashAttribute("error", "해당 주소의 좌표를 찾을 수 없습니다. 주소를 다시 확인해주세요.");
	        return "redirect:/members/me/addresses/new"; // 등록 폼으로 돌려보냄
	    }
	    
		return "redirect:/members/me/addresses";
	}
	
	// 주소 등록 폼 띄우기
	@GetMapping("/members/me/addresses/new")
	public String insertAddress() {
		
		return "views/address-create";
	}
	
	// 기본 배송지 변경
	@PostMapping("/members/me/addresses/{deliveryAddressIdx}/default")
	public String changeDefaultAddress(
	        @PathVariable("deliveryAddressIdx") int deliveryAddressIdx,
	        @SessionAttribute("memberIdx") int memberIdx,
	        @RequestParam(value = "redirectUrl", defaultValue = "/members/me/addresses") String redirectUrl,
	        HttpSession session,
	        RedirectAttributes ra) {

	    try {
	        deliveryAddressService.changeDefaultAddress(memberIdx, deliveryAddressIdx);

	        // 헤더에서 쓰는 현재 선택 배송지도 같이 동기화
	        session.setAttribute("selected_address_idx", deliveryAddressIdx);

	        ra.addFlashAttribute("message", "기본 배송지가 변경되었습니다.");
	    } catch (RuntimeException e) {
	        ra.addFlashAttribute("error", e.getMessage());
	    }

	    return "redirect:" + redirectUrl;
	}
	
	// 배송지 선택 (주문 시)
	@PostMapping("/members/me/addresses/{deliveryAddressIdx}/select")
	@ResponseBody
	public String selectAddress(
	        @PathVariable int deliveryAddressIdx,
	        @SessionAttribute("memberIdx") int memberIdx,
	        HttpSession session) {

	    // 선택한 주소를 세션에 저장 (예시)
	    session.setAttribute("selected_address_idx", deliveryAddressIdx);

	    return "ok";
	}
	
	// 주소 리스트 폼 띄우기
	// 헤더 반영 위해서 수정 -영훈
	@GetMapping("/members/me/addresses")
	public String addressList(Model model,
	        @SessionAttribute("memberIdx") int memberIdx) {

	    List<DeliveryAddress> addressList = deliveryAddressService.getAddress(memberIdx);
	    Integer defaultAddressIdx = deliveryAddressService.getDefaultAddressIdx(memberIdx);

	    model.addAttribute("addressList", addressList);
	    model.addAttribute("defaultAddressIdx", defaultAddressIdx);

	    return "views/address-list";
	}
	
	// 헤더에서 배송지 선택
	@PostMapping("/members/me/addresses/{deliveryAddressIdx}/select-from-header")
	public String selectAddressFromHeader(
	        @PathVariable("deliveryAddressIdx") int deliveryAddressIdx,
	        @SessionAttribute("memberIdx") int memberIdx,
	        HttpSession session,
	        jakarta.servlet.http.HttpServletRequest request,
	        RedirectAttributes ra) {

	    try {
	        // 실제 기본 배송지 DB 변경
	        deliveryAddressService.changeDefaultAddress(memberIdx, deliveryAddressIdx);

	        // 헤더 현재 선택도 즉시 동기화
	        session.setAttribute("selected_address_idx", deliveryAddressIdx);

	        ra.addFlashAttribute("message", "기본 배송지가 변경되었습니다.");
	    } catch (RuntimeException e) {
	        ra.addFlashAttribute("error", e.getMessage());
	    }

	    String referer = request.getHeader("Referer");
	    if (referer == null || referer.isBlank()) {
	        return "redirect:/";
	    }

	    return "redirect:" + referer;
	}
	
	
	
}
