package com.moeats.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.moeats.domain.GroupOrder;
import com.moeats.domain.GroupOrderItem;
import com.moeats.domain.Member;
import com.moeats.domain.Payment;
import com.moeats.domain.PaymentShare;
import com.moeats.domain.RoomParticipant;
import com.moeats.services.GroupOrderService;
import com.moeats.services.OrderMemberQueryService;
import com.moeats.services.OrderRoomService;
import com.moeats.services.PaymentService;
import com.moeats.services.sse.SSEService;
import com.moeats.timer.OrderRoomTimer;


@Controller
public class OrderController {
	@Autowired
    DeliveryAddressController deliveryAddressController;
	@Autowired
	GroupOrderService groupOrderService;
	@Autowired
	OrderRoomService orderRoomService;
	@Autowired
	PaymentService paymentService;
	@Autowired
	OrderMemberQueryService memberService;
	
	@Autowired
	SSEService sseService;
	@Autowired
	OrderRoomTimer orderRoomTimer;
	
	@GetMapping("/orders/{order_idx}")
	public String orderDetail(
			Model model,
			@PathVariable("order_idx") int orderIdx,
			@RequestAttribute("groupOrder") GroupOrder groupOrder) {
		final record MemberItems(RoomParticipant roomParticipant,Member member,List<GroupOrderItem> items) {}

		Map<Integer,List<GroupOrderItem>> groupOrderItems = groupOrderService.findByOrder(groupOrder.getOrderIdx())
				.stream().collect(Collectors.groupingBy(GroupOrderItem::getMemberIdx));
		Map<Integer,RoomParticipant> roomParticipantMap = orderRoomService.findByRoom(groupOrder.getRoomIdx())
				.stream().collect(Collectors.toMap(RoomParticipant::getMemberIdx, roomParticipant -> roomParticipant));
		
		List<MemberItems> memberItems = memberService.findByIdxs(roomParticipantMap.keySet())
				.stream().map(roomMember->new MemberItems(
						roomParticipantMap.get(roomMember.getMemberIdx()),
						roomMember,
						groupOrderItems.getOrDefault(roomMember.getMemberIdx(), List.of()))
				).toList();
		// request.setAttribute 되어 있으면 model에서 다시 넣을 필요 없음 (완전히 동일)
//		model.addAttribute("isOwner", isOwner);
//		model.addAttribute("groupOrder",groupOrder);
		model.addAttribute("memberItems", memberItems);
		return "order-detail";
	}
	
	@GetMapping("/orders/{order_idx}/payment")
	public String orderPayment(
			RedirectAttributes ra,
			Model model,
			@PathVariable("order_idx") int orderIdx,
			@RequestAttribute("groupOrder") GroupOrder groupOrder,
			@RequestAttribute("payment") Payment payment,
			@SessionAttribute("member") Member member) {
		PaymentShare paymentShare = paymentService.findPaymentMember(payment.getPaymentIdx(), member.getMemberIdx());
		String destination = "individual";
		if( paymentShare==null ) {
			// 존재해서는 안 되는 오류
			ra.addFlashAttribute("error", "오류가 발생했습니다 관리자에게 연락해주십시오");
			return "redirect:/main";
		}
		if( payment.getPaymentStatus().equals("PAID"))
			return String.format("redirect:/orders/%d/status",orderIdx);
		if( paymentShare.getShareStatus().equals("PAID_SELF") )
			destination = "wait";
		else if( groupOrder.getPaymentMode().equals("REPRESENTATIVE") && groupOrder.getLeaderMemberIdx() == member.getMemberIdx() )
			destination = "representative";
		return String.format("redirect:/orders/%d/payment/%s",orderIdx,destination);
		// 자동 분기라면 view가 필요 없을 것
		//return "payment-entry";
	}
	@GetMapping("/orders/{order_idx}/payment/representative")
	public String representativePay(
			@PathVariable("order_idx") int orderIdx,
			@RequestAttribute("groupOrder") GroupOrder groupOrder,
			@RequestAttribute("paymentShare") PaymentShare paymentShare,
			@SessionAttribute("member") Member member) {
		if( member.getMemberIdx()!=groupOrder.getLeaderMemberIdx() )
			return String.format("redirect:/orders/%d/payment/wait",orderIdx);
		else if( paymentShare.getShareStatus().equals("PAID_SELF") )
			return String.format("redirect:/orders/%d/status",orderIdx);
		return "payment-representative";
	}
	@GetMapping("/orders/{order_idx}/payment/individual")
	public String individualPay(
			@PathVariable("order_idx") int orderIdx,
			@RequestAttribute("paymentShare") PaymentShare paymentShare) {
		if( paymentShare.getShareStatus().equals("PAID_SELF") )
			return String.format("redirect:/orders/%d/payment/wait",orderIdx);
		return "payment-individual";
	}
	@GetMapping("/orders/{order_idx}/payment/wait")
	public String waitForPayment(
			Model model,
			@PathVariable("order_idx") int orderIdx,
			@RequestAttribute("payment") Payment payment,
			@RequestAttribute("paymentShare") PaymentShare paymentShare,
			@SessionAttribute("member") Member member) {
		if( paymentShare.getShareStatus().equals("PENDING") && payment.getPaymentMode().equals("INDIVIDUAL") )
			return String.format("redirect:/orders/%d/payment",orderIdx);
		if( paymentShare.getShareStatus().equals("CANCELLED") )
			return String.format("redirect:/orders/%d",orderIdx);
		List<PaymentShare> paymentShares = paymentService.findByPayment(payment.getPaymentIdx());
		model.addAttribute("paymentShares", paymentShares);
		return "payment-individual";
	}
	@PostMapping("/orders/{order_idx}/payment/complete")
	public String paymentComplete(
			RedirectAttributes ra,
			@PathVariable("order_idx") int orderIdx,
			@RequestAttribute("groupOrder") GroupOrder groupOrder,
			@RequestAttribute("payment") Payment payment,
			@RequestAttribute("paymentShare") PaymentShare paymentShare,
			@SessionAttribute("member") Member member) {
		// TODO 여기서 어떤 정보를 받는지, 어떻게 결제했는지 체크할지
		int paymentShareIdx = paymentShare.getPaymentShareIdx();
 		if( paymentService.paySelf(paymentShareIdx)==0 ) {
			ra.addFlashAttribute("error", "결제 중 오류가 발생했습니다 관리자에게 연락해주십시오");
			return String.format("redirect:/orders/%d/payment",orderIdx);
 		}
 		if( payment.getPaymentMode().equals("REPRESENTATIVE") ) {
 			if( member.getMemberIdx()!=groupOrder.getLeaderMemberIdx() ) {
 				ra.addFlashAttribute("error", "잘못된 접근입니다");
 				return "redirect:/main";
 			}
 			paymentShare = paymentService.findShareByIdx(paymentShareIdx);
 			paymentService.paidByRepresentative(payment.getPaymentIdx(),paymentShare.getPaidAt());
 			orderRoomTimer.stop(orderIdx);
 			sseService.payOrder(orderIdx, paymentShareIdx);
 			sseService.payComplete(orderIdx,groupOrder.getStoreIdx());
 			return String.format("redirect:/orders/%d/status",orderIdx);
 		}
		if( paymentService.findPaymentPending(payment.getPaymentIdx()).isEmpty() ){
		    orderRoomTimer.stop(orderIdx);
		    sseService.payOrder(orderIdx, paymentShareIdx);
		    sseService.payComplete(orderIdx,groupOrder.getStoreIdx());
		    return String.format("redirect:/orders/%d/status",orderIdx);
		}
		sseService.payOrder(orderIdx, paymentShareIdx);
		return String.format("redirect:/orders/%d/payment/wait",orderIdx);
	}
}
