package com.moeats.controller.rest;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.Member;
import com.moeats.domain.OrderRoom;
import com.moeats.services.OrderRoomService;

@RestController
public class RoomRestController {
	@Autowired
	OrderRoomService orderRoomService;
	
	@PostMapping("/rooms/code/{room_code}/select")
	public Map selectRoom(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		if( orderRoom.isJoinLocked() || orderRoomService.setSelect(member.getMemberIdx())==0 )
			return Map.of("result","failed");
		return Map.of("result","success");
	}
	@PostMapping("/rooms/code/{room_code}/unselect")
	public Map unselectRoom(
			RedirectAttributes ra,
			@PathVariable("room_code") String roomCode,
			@RequestAttribute("orderRoom") OrderRoom orderRoom,
			@SessionAttribute("member") Member member) {
		if( orderRoom.isJoinLocked() || orderRoomService.unselect(member.getMemberIdx())==0 )
			return Map.of("result","failed");
		return Map.of("result","success");
	}
}
