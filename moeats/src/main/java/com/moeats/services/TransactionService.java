package com.moeats.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moeats.domain.GroupOrder;
import com.moeats.domain.OrderDelivery;
import com.moeats.domain.OrderRoom;
import com.moeats.domain.Payment;
import com.moeats.domain.PaymentShare;

@Service
public class TransactionService {
	@Autowired
	OrderRoomService orderRoomService;
	@Autowired
	GroupOrderService groupOrderService;
	@Autowired
	PaymentService paymentService;
	@Autowired
	GroupCartItemService groupCartItemService;
	@Autowired
	MemberService memberService;
	
	@Transactional
	public Map<String,Object> beginPayment(OrderRoom orderRoom,int representativeMemberIdx) {
		boolean representative = orderRoom.getPaymentMode().equals("REPRESENTATIVE");
		if(	representative && orderRoomService.findRoomMember(orderRoom.getRoomIdx(),representativeMemberIdx)==null)
			// representativeMemberIdx = orderRoom.getLeaderMemberIdx();
			return null;
		Map<String,Object> map = new HashMap<>();
		GroupOrder groupOrder = new GroupOrder();
		Payment payment = new Payment();
		OrderDelivery orderDelivery = new OrderDelivery();
		
		groupOrder.setFrom(orderRoom);
		groupOrder.setOrderTotalAmount(groupCartItemService.findRoomAmount(orderRoom.getRoomIdx()));
		groupOrderService.insert(groupOrder);
		
		payment.setFrom(groupOrder);
		paymentService.insert(payment);
		
		List<PaymentShare> paymentShares = new ArrayList<>();
		if(representative)
			paymentService.setRepresentativePaymentShares(paymentShares, payment, orderRoomService.findByRoom(orderRoom.getRoomIdx()), representativeMemberIdx);
		else
			paymentService.setIndividualPaymentShares(paymentShares, payment, groupCartItemService.findRoomMemberAmount(orderRoom.getRoomIdx()));
		paymentShares.forEach(paymentShare->paymentService.insert(paymentShare));
		orderDelivery.setFrom(groupOrder.getOrderIdx(),
				memberService.findAddressByIdx(orderRoom.getSelectedDeliveryAddressIdx()));
		groupOrderService.insertDelivery(orderDelivery);
		
		Timestamp expiresAt = payment.getPaymentExpiresAt();
		orderRoomService.paymentPend(orderRoom.getRoomIdx(), expiresAt);
		orderRoom.setExpiresAt(expiresAt);
		
		map.put("groupOrder",groupOrder);
		map.put("payment",payment);
		map.put("paymentShares",paymentShares);
		map.put("orderDelivery",orderDelivery);
		return map;
	}
}
