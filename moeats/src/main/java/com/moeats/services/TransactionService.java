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
import com.moeats.services.sse.SSEService;

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
	OrderMemberQueryService memberService;
	
	@Autowired
	SSEService sseService;
	
	@Transactional(rollbackFor=Exception.class)
	public Map<String,Object> beginPayment(OrderRoom orderRoom) throws Exception {
		boolean isRepresentative = orderRoom.getPaymentMode().equals("REPRESENTATIVE");
		DeliveryAddress deliveryAddress = null;
		if(orderRoom.getOrderMode().equals("DELIVERY")) {
			deliveryAddress = memberService.findAddressByIdx(orderRoom.getSelectedDeliveryAddressIdx());
			if(	deliveryAddress==null || deliveryAddress.getMemberIdx() != orderRoom.getLeaderMemberIdx() )
				// representativeMemberIdx = orderRoom.getLeaderMemberIdx();
				throw new Exception();
		}
		Map<String,Object> map = new HashMap<>();
		GroupOrder groupOrder = GroupOrder.from(orderRoom);
		groupOrder.setOrderTotalAmount(groupCartItemService.findRoomAmount(orderRoom.getRoomIdx()));
		groupOrderService.insert(groupOrder);
		
		Payment payment = Payment.from(groupOrder);
		paymentService.insert(payment);
		
		List<PaymentShare> paymentShares = new ArrayList<>();
		if(isRepresentative)
			paymentService.setRepresentativePaymentShares(paymentShares, payment, orderRoomService.findByRoom(orderRoom.getRoomIdx()),orderRoom.getLeaderMemberIdx());
		else
			paymentService.setIndividualPaymentShares(paymentShares, payment, groupCartItemService.findRoomMemberAmount(orderRoom.getRoomIdx()));
		paymentShares.forEach(paymentShare->paymentService.insert(paymentShare));
		if(!isRepresentative)
			paymentShares.stream().filter(paymentShare->paymentShare.getShareAmount()==0)
				.forEach(paymentShare->paymentService.paySelf(paymentShare.getPaymentShareIdx()));
		OrderDelivery orderDelivery = null;
		if(deliveryAddress != null) {
			orderDelivery = OrderDelivery.from(groupOrder.getOrderIdx(),deliveryAddress);
			groupOrderService.insertDelivery(orderDelivery);
		}
		
		Timestamp expiresAt = payment.getPaymentExpiresAt();
		orderRoomService.paymentPend(orderRoom.getRoomIdx(), expiresAt);
		orderRoom.setExpiresAt(expiresAt);
		
		map.put("groupOrder",groupOrder);
		map.put("payment",payment);
		map.put("paymentShares",paymentShares);
		map.put("orderDelivery",orderDelivery);
		return map;
	}
@Transactional(rollbackFor=Exception.class)
public int revertToSelect(OrderRoom orderRoom) {
        if (orderRoom == null || !"PAYMENT_PENDING".equals(orderRoom.getRoomStatus()))
                return 0;

        GroupOrder groupOrder = groupOrderService.findByRoom(orderRoom.getRoomIdx());
        if (groupOrder == null)
                return 0;

        Payment payment = paymentService.findByOrder(groupOrder.getOrderIdx());
        if (payment == null || !paymentService.findPaymentPaidSelf(payment.getPaymentIdx()).isEmpty())
                return 0;

        paymentService.delete(payment.getPaymentIdx());
        groupOrderService.delete(groupOrder.getOrderIdx());
        return orderRoomService.revertToSelect(orderRoom.getRoomIdx());
}

	@Transactional(rollbackFor=Exception.class)
	public int expirePayment(int orderIdx) {
		GroupOrder groupOrder = groupOrderService.findByIdx(orderIdx);
		if(groupOrder == null)
			return 0;
		
		Payment payment = paymentService.findByOrder(orderIdx);
		if(payment == null)
			return 0;
		
		int result = 0;
		result += paymentService.cancel(payment.getPaymentIdx());
		result += groupOrderService.cancel(orderIdx);
		result += orderRoomService.expire(groupOrder.getRoomIdx());
		sseService.expireOrder(orderIdx);
		return result;
	}
}
