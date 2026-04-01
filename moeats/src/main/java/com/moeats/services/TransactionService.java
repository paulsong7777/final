package com.moeats.services;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moeats.domain.DeliveryAddress;
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
	
	@Transactional(rollbackFor=Exception.class)
	public Map<String,Object> beginPayment(OrderRoom orderRoom,int representativeMemberIdx) throws Exception {
		boolean isRepresentative = orderRoom.getPaymentMode().equals("REPRESENTATIVE");
		DeliveryAddress deliveryAddress = memberService.findAddressByIdx(orderRoom.getSelectedDeliveryAddressIdx());
		if(	isRepresentative && orderRoomService.findRoomMember(orderRoom.getRoomIdx(),representativeMemberIdx)==null ||
			deliveryAddress==null || deliveryAddress.getMemberIdx() != orderRoom.getLeaderMemberIdx() )
			// representativeMemberIdx = orderRoom.getLeaderMemberIdx();
			throw new Exception();
		Map<String,Object> map = new HashMap<>();
		GroupOrder groupOrder = GroupOrder.from(orderRoom);
		groupOrder.setOrderTotalAmount(groupCartItemService.findRoomAmount(orderRoom.getRoomIdx()));
		groupOrderService.insert(groupOrder);
		
		Payment payment = Payment.from(groupOrder);
		paymentService.insert(payment);
		
		List<PaymentShare> paymentShares = new ArrayList<>();
		if(isRepresentative)
			paymentService.setRepresentativePaymentShares(paymentShares, payment, orderRoomService.findByRoom(orderRoom.getRoomIdx()), representativeMemberIdx);
		else
			paymentService.setIndividualPaymentShares(paymentShares, payment, groupCartItemService.findRoomMemberAmount(orderRoom.getRoomIdx()));
		paymentShares.forEach(paymentShare->paymentService.insert(paymentShare));
		
		OrderDelivery orderDelivery = OrderDelivery.from(groupOrder.getOrderIdx(),deliveryAddress);
		groupOrderService.insertDelivery(orderDelivery);
		
		Timestamp expiresAt = payment.getPaymentExpiresAt();
		orderRoomService.paymentPend(orderRoom.getRoomIdx(), expiresAt);
		orderRoom.setExpiresAt(expiresAt);
		
		assert groupOrder!=null && payment!=null && paymentShares!=null && orderDelivery!=null;
		map.put("groupOrder",groupOrder);
		map.put("payment",payment);
		map.put("paymentShares",paymentShares);
		map.put("orderDelivery",orderDelivery);
		return map;
	}
	@Transactional(rollbackFor=Exception.class)
	public int revertToSelect(OrderRoom orderRoom) {
		// 결제한 사람이 없을때만 가능
		Payment payment = paymentService.findByOrder(orderRoom.getRoomIdx());
		if(		orderRoom==null	||!orderRoom.getRoomStatus().equals("PAYMENT_PENDING")||
				payment==null	||!paymentService.findPaymentPaidSelf(payment.getPaymentIdx()).isEmpty())
			return 0;
		paymentService.delete(payment.getPaymentIdx());
		groupOrderService.delete(orderRoom.getRoomIdx());
		return orderRoomService.revertToSelect(orderRoom.getRoomIdx());
	}
}
