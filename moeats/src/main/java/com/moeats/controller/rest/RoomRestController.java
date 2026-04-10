package com.moeats.controller.rest;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.GroupCartItem;
import com.moeats.domain.Member;
import com.moeats.domain.OrderRoom;
import com.moeats.domain.RoomParticipant;
import com.moeats.services.GroupCartItemService;
import com.moeats.services.OrderRoomService;

@RestController
public class RoomRestController {
	@Autowired
	private GroupCartItemService groupCartItemService;

	@PostMapping("/rooms/code/{room_code}/cart")
	public Map roomCartAdd(
			@ModelAttribute GroupCartItem groupCartItem,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		groupCartItem.setMemberIdx(member.getMemberIdx());
		groupCartItem.setRoomIdx(orderRoom.getRoomIdx());
		if (	orderRoom.isJoinLocked()
				|| groupCartItemService.insert(groupCartItem) == 0)
			return Map.of("result", false);
		return Map.of("result", true);
	}

	@GetMapping("/rooms/code/{room_code}/cart/get")
	@ResponseBody
	public List<GroupCartItem> getMyCartItems(
	        @PathVariable("room_code") String roomCode,
	        @RequestAttribute("orderRoom") OrderRoom orderRoom,
	        @SessionAttribute("member") Member member) {
	    
		System.out.println("===> 장바구니 조회 요청 도달!");
		
	    // 1. 현재 접속한 유저(member)가 이 방(roomCode)에 담아둔 장바구니 목록을 DB에서 조회
	    // 2. JSON 형태로 반환
	    return groupCartItemService.findRoomMember(orderRoom.getRoomIdx(), member.getMemberIdx());
	}
	
	@PostMapping("/rooms/code/{room_code}/cart/items/{cart_item_idx}/edit")
	public Map cartItemEdit(
			@ModelAttribute GroupCartItem groupCartItem,
			@PathVariable("room_code") String roomCode,
			@PathVariable("cart_item_idx") int cartItemIdx,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		GroupCartItem preExist = groupCartItemService.findByIdx(cartItemIdx);
		groupCartItem.setCartItemIdx(cartItemIdx);
		groupCartItem.setRoomIdx(orderRoom.getRoomIdx());
		groupCartItem.setMemberIdx(member.getMemberIdx());
		if (	orderRoom.isJoinLocked()
				|| preExist == null
				|| preExist.getRoomIdx() != orderRoom.getRoomIdx()
				|| preExist.getMemberIdx() != member.getMemberIdx()
				|| groupCartItemService.update(groupCartItem) == 0)
			return Map.of("result", false);
		return Map.of("result", true);
	}

	@PostMapping("/rooms/code/{room_code}/cart/items/{cart_item_idx}/delete")
	public Map cartItemDelete(
			@PathVariable("room_code") String roomCode,
			@PathVariable("cart_item_idx") int cartItemIdx,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		GroupCartItem groupCartItem = groupCartItemService.findByIdx(cartItemIdx);
		if (	orderRoom.isJoinLocked()
				|| groupCartItem == null
				|| groupCartItem.getRoomIdx() != orderRoom.getRoomIdx()
				|| groupCartItem.getMemberIdx() != member.getMemberIdx()
				|| groupCartItemService.remove(cartItemIdx) == 0)
			return Map.of("result", false);
		return Map.of("result", true);
	}
}
