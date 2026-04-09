package com.moeats.controller.rest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.moeats.domain.GroupOrder;
import com.moeats.domain.Member;
import com.moeats.domain.OrderRoom;
import com.moeats.domain.Store;
import com.moeats.service.StoreService;
import com.moeats.services.sse.SSEService;

@RestController
public class SseController {
	@Autowired
	StoreService storeService;
	@Autowired
	SSEService sseService;
	@GetMapping("/rooms/code/{room_code}/subscribe")
	public SseEmitter subscribeRoom(@RequestAttribute("orderRoom") OrderRoom orderRoom) {
		return sseService.joinRoom(orderRoom.getRoomIdx());
	}
	@GetMapping("/orders/{order_idx}/subscribe")
	public SseEmitter subscribeOrder(@RequestAttribute("groupOrder") GroupOrder groupOrder) {
		return sseService.joinOrder(groupOrder.getOrderIdx());
	}
	@GetMapping("/owners/order/subscribe")
	public SseEmitter subscribeOrder(@SessionAttribute("member") Member member) {
    	Store store = storeService.myStore(member.getMemberIdx());
    	if ( store==null ) {
    		throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    	}
		return sseService.joinStore(store.getStoreIdx());
	}
}