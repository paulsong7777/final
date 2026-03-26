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

import com.moeats.domain.DeliveryAddress;
import com.moeats.service.DeliveryAddressService;

import jakarta.servlet.http.HttpSession;

@Controller
public class DeliveryAddressController {
	
	@Autowired
	private DeliveryAddressService deliveryAddressService;
	

	
	// 삭제
	@PostMapping("/members/me/addresses/{deliveryAddressIdx}/delete")
	public String deleteAddress( @PathVariable("deliveryAddressIdx") int deliveryAddressIdx,
	        @SessionAttribute("memberIdx") int memberIdx) {

		deliveryAddressService.deleteAddress(memberIdx, deliveryAddressIdx);
		
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

	    return "views/address-edit";
	}
	
	
	// 등록
	@PostMapping("/members/me/addresses")
	public String insertAddress(@ModelAttribute DeliveryAddress deliveryAddress,
	        @SessionAttribute("memberIdx") int memberIdx) {
		
	    deliveryAddress.setMemberIdx(memberIdx);

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
	        @SessionAttribute("memberIdx") int memberIdx) {

	    deliveryAddressService.changeDefaultAddress(memberIdx, deliveryAddressIdx);

	    return "ok";
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
	@GetMapping("/members/me/addresses")
	public String addressList(Model model,
	        @SessionAttribute("memberIdx") int memberIdx) {

	    List<DeliveryAddress> addressList = deliveryAddressService.getAddress(memberIdx);

	    model.addAttribute("addressList", addressList);

	    return "views/address-list";
	}
	
}
