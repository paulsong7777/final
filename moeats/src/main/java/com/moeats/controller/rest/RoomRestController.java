package com.moeats.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.moeats.domain.OrderRoom;
import com.moeats.services.sse.SSEService;

@RestController
public class RoomRestController {
	@Autowired
	SSEService sseService;
	@GetMapping("/rooms/code/{room_code}/subscribe")
	public SseEmitter subscribeRoom(@RequestAttribute("orderRoom") OrderRoom orderRoom) {
		return sseService.join(orderRoom.getRoomIdx());
	}
}