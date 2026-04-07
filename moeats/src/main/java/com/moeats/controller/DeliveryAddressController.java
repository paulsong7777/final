package com.moeats.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.DeliveryAddress;
import com.moeats.domain.Member;
import com.moeats.service.DeliveryAddressService;

import jakarta.servlet.http.HttpSession;

@Controller
public class DeliveryAddressController {
	@Autowired
	private DeliveryAddressService deliveryAddressService;
	
	// 삭제
	@PostMapping("/members/me/addresses/{deliveryAddressIdx}/delete")
	public String deleteAddress(
			RedirectAttributes ra,
			@PathVariable("deliveryAddressIdx") int deliveryAddressIdx,
	        @SessionAttribute("member") Member member) {
		DeliveryAddress deliveryAddress = deliveryAddressService.addressByIdx(member.getMemberIdx(),deliveryAddressIdx);
		if(deliveryAddress==null) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return "redirect:/home";
		}
		deliveryAddressService.deleteAddress(member.getMemberIdx(), deliveryAddressIdx);
		
		return "redirect:/members/me/addresses";
	}

	
	// 수정
	@PostMapping("/members/me/addresses/{deliveryAddressIdx}/edit")
	public String updateAddress(
			RedirectAttributes ra,
	        @ModelAttribute DeliveryAddress deliveryAddress,
	        @SessionAttribute("member") Member member) {
		DeliveryAddress pre = deliveryAddressService.addressByIdx(member.getMemberIdx(),deliveryAddress.getDeliveryAddressIdx());
		if(pre==null) {
			ra.addFlashAttribute("error", "잘못된 접근입니다");
			return "redirect:/home";
		}

	    deliveryAddressService.updateAddress(deliveryAddress);

	    return "redirect:/members/me/addresses";
	}
	
	// 주소 수정 폼 띄우기
	@GetMapping("/members/me/addresses/{deliveryAddressIdx}/edit")
	public String updateAddressForm(
	        Model model,
	        @PathVariable("deliveryAddressIdx") int deliveryAddressIdx,
	        @SessionAttribute("member") Member member) {

	    DeliveryAddress address = deliveryAddressService.addressByIdx(member.getMemberIdx(), deliveryAddressIdx);

	    model.addAttribute("address", address);

	    return "views/members/address-edit";
	}
	
	
	// 등록
	@PostMapping("/members/me/addresses")
	public String insertAddress(
			@ModelAttribute DeliveryAddress deliveryAddress,
	        @SessionAttribute("member") Member member) {
		
	    deliveryAddress.setMemberIdx(member.getMemberIdx());

	    deliveryAddressService.insertAddress(deliveryAddress);
	    
		return "redirect:/members/me/addresses";
	}
	
	// 주소 등록 폼 띄우기
	@GetMapping("/members/me/addresses/new")
	public String insertAddress() {
		
		return "views/address-create";
	}
	
	// 기본 배송지 변경
	@PostMapping("/members/me/addresses/{deliveryAddressIdx}/default")
	@ResponseBody
	public String changeDefaultAddress(
	        @PathVariable("deliveryAddressIdx") int deliveryAddressIdx,
	        @SessionAttribute("member") Member member) {

	    deliveryAddressService.changeDefaultAddress(member.getMemberIdx(), deliveryAddressIdx);

	    return "ok";
	}
	
	// 배송지 선택 (주문 시)
	@PostMapping("/members/me/addresses/{deliveryAddressIdx}/select")
	@ResponseBody
	public String selectAddress(
	        @PathVariable int deliveryAddressIdx,
	        @SessionAttribute("member") Member member,
	        HttpSession session) {

	    // 선택한 주소를 세션에 저장 (예시)
	    session.setAttribute("selected_address_idx", deliveryAddressIdx);

	    return "ok";
	}
	
	// 주소 리스트 폼 띄우기
	@GetMapping("/members/me/addresses")
	public String addressList(
			Model model,
	        @SessionAttribute("member") Member member) {
		
	    List<DeliveryAddress> addressList = deliveryAddressService.getAddress(member.getMemberIdx());
	    
	    model.addAttribute("addressList", addressList);
	    
	    return "views/address-list";
	}
	
}
