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
import com.moeats.domain.RoomParticipant;
import com.moeats.services.sse.SSEService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	
	@Transactional(rollbackFor = Exception.class)
	public Map<String, Object> beginPayment(OrderRoom orderRoom) throws Exception {
	    boolean isRepresentative = orderRoom.getPaymentMode().equals("REPRESENTATIVE");
	    DeliveryAddress deliveryAddress = null;

	    log.info(
	        "beginPayment start roomIdx={}, roomStatus={}, paymentMode={}, orderMode={}, leaderMemberIdx={}, selectedDeliveryAddressIdx={}",
	        orderRoom.getRoomIdx(),
	        orderRoom.getRoomStatus(),
	        orderRoom.getPaymentMode(),
	        orderRoom.getOrderMode(),
	        orderRoom.getLeaderMemberIdx(),
	        orderRoom.getSelectedDeliveryAddressIdx()
	    );

	    if (orderRoom.getOrderMode().equals("DELIVERY")) {
	        deliveryAddress = memberService.findAddressByIdx(orderRoom.getSelectedDeliveryAddressIdx());

	        log.info(
	            "deliveryAddress loaded deliveryAddressIdx={}, memberIdx={}",
	            deliveryAddress != null ? deliveryAddress.getDeliveryAddressIdx() : null,
	            deliveryAddress != null ? deliveryAddress.getMemberIdx() : null
	        );

	        if (deliveryAddress == null || deliveryAddress.getMemberIdx() != orderRoom.getLeaderMemberIdx()) {
	            log.error(
	                "deliveryAddress validation failed. roomIdx={}, leaderMemberIdx={}, selectedDeliveryAddressIdx={}",
	                orderRoom.getRoomIdx(),
	                orderRoom.getLeaderMemberIdx(),
	                orderRoom.getSelectedDeliveryAddressIdx()
	            );
	            throw new Exception();
	        }
	    }

	    Map<String, Object> map = new HashMap<>();

	    GroupOrder groupOrder = GroupOrder.from(orderRoom);
	    groupOrder.setOrderTotalAmount(groupCartItemService.findRoomAmount(orderRoom.getRoomIdx()));

	    log.info(
	        "before groupOrder insert roomIdx={}, storeIdx={}, leaderMemberIdx={}, orderTotalAmount={}",
	        groupOrder.getRoomIdx(),
	        groupOrder.getStoreIdx(),
	        groupOrder.getLeaderMemberIdx(),
	        groupOrder.getOrderTotalAmount()
	    );

	    groupOrderService.insert(groupOrder);

	    log.info("groupOrder inserted orderIdx={}", groupOrder.getOrderIdx());

	    Payment payment = Payment.from(groupOrder);

	    log.info(
	        "before payment insert orderIdx={}, paymentMode={}, paymentRequestAmount={}",
	        payment.getOrderIdx(),
	        payment.getPaymentMode(),
	        payment.getPaymentRequestAmount()
	    );

	    Timestamp expiresAt = Timestamp.from(java.time.Instant.now().plus(java.time.Duration.ofMinutes(5)));
	    payment.setPaymentExpiresAt(expiresAt);
	    paymentService.insert(payment);

	 // insert 직후 DB에서 다시 읽어서 실제 payment_idx 확보
	 payment = paymentService.findByOrder(groupOrder.getOrderIdx());

	 if (payment == null || payment.getPaymentIdx() <= 0) {
	     throw new IllegalStateException("payment 생성 후 payment_idx 조회 실패");
	 }

	 log.info(
	     "payment inserted paymentIdx={}, orderIdx={}",
	     payment.getPaymentIdx(),
	     payment.getOrderIdx()
	 );

	    List<PaymentShare> paymentShares = new ArrayList<>();
	    if (isRepresentative) {
	        paymentService.setRepresentativePaymentShares(
	            paymentShares,
	            payment,
	            orderRoomService.findByRoom(orderRoom.getRoomIdx()),
	            orderRoom.getLeaderMemberIdx()
	        );
	    } else {
	        paymentService.setIndividualPaymentShares(
	            paymentShares,
	            payment,
	            groupCartItemService.findRoomMemberAmount(orderRoom.getRoomIdx())
	        );
	    }

	    log.info("paymentShares prepared count={}", paymentShares.size());

	    paymentShares.forEach(paymentShare -> {
	        log.info(
	            "before paymentShare insert paymentIdx={}, memberIdx={}, shareAmount={}",
	            paymentShare.getPaymentIdx(),
	            paymentShare.getMemberIdx(),
	            paymentShare.getShareAmount()
	        );
	        paymentService.insert(paymentShare);
	    });

	    if (!isRepresentative) {
	        paymentShares.stream()
	            .filter(paymentShare -> paymentShare.getShareAmount() == 0)
	            .forEach(paymentShare -> paymentService.paySelf(paymentShare.getPaymentShareIdx()));
	    }

	    OrderDelivery orderDelivery = null;
	    if (deliveryAddress != null) {
	        orderDelivery = OrderDelivery.from(groupOrder.getOrderIdx(), deliveryAddress);

	        log.info(
	            "before orderDelivery insert orderIdx={}, sourceDeliveryAddressIdx={}",
	            orderDelivery.getOrderIdx(),
	            orderDelivery.getSourceDeliveryAddressIdx()
	        );

	        groupOrderService.insertDelivery(orderDelivery);

	        log.info("orderDelivery inserted orderIdx={}", orderDelivery.getOrderIdx());
	    }

	    orderRoomService.paymentPend(orderRoom.getRoomIdx(), expiresAt);
	    orderRoom.setExpiresAt(expiresAt);

	    log.info(
	        "before paymentPend roomIdx={}, expiresAt={}",
	        orderRoom.getRoomIdx(),
	        expiresAt
	    );



	    log.info(
	        "beginPayment success roomIdx={}, orderIdx={}, paymentIdx={}",
	        orderRoom.getRoomIdx(),
	        groupOrder.getOrderIdx(),
	        payment.getPaymentIdx()
	    );

	    map.put("groupOrder", groupOrder);
	    map.put("payment", payment);
	    map.put("paymentShares", paymentShares);
	    map.put("orderDelivery", orderDelivery);
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

	@Transactional(rollbackFor = Exception.class)
	public boolean completePayment(GroupOrder groupOrder, Payment payment, PaymentShare paymentShare) {
		if (groupOrder == null || payment == null || paymentShare == null) {
			return false;
		}

		if (paymentService.paySelf(paymentShare.getPaymentShareIdx()) == 0) {
			return false;
		}

		RoomParticipant roomParticipant = orderRoomService.findRoomMember(
				groupOrder.getRoomIdx(),
				paymentShare.getMemberIdx());
		if (roomParticipant != null && "UNPAID".equals(roomParticipant.getPaymentStatus())) {
			orderRoomService.pay(roomParticipant.getRoomParticipantIdx());
		}

		if ("REPRESENTATIVE".equals(payment.getPaymentMode())) {
			PaymentShare paidShare = paymentService.findShareByIdx(paymentShare.getPaymentShareIdx());
			paymentService.paidByRepresentative(payment.getPaymentIdx(), paidShare.getPaidAt());

			orderRoomService.findByRoom(groupOrder.getRoomIdx()).stream()
					.filter(participant -> "UNPAID".equals(participant.getPaymentStatus()))
					.forEach(participant -> orderRoomService.pay(participant.getRoomParticipantIdx()));
		}

		if (paymentService.findPaymentPending(payment.getPaymentIdx()).isEmpty()) {
			paymentService.pay(payment.getPaymentIdx());
			groupOrderService.pay(groupOrder.getOrderIdx());
			orderRoomService.confirm(groupOrder.getRoomIdx());
		}

		return true;
	}
}
