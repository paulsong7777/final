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
import org.springframework.web.bind.annotation.ResponseBody;

import com.moeats.mapper.StoreMapper;
import com.moeats.service.DeliveryAddressService;
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
import com.moeats.services.TransactionService;
import com.moeats.services.sse.SSEService;
import com.moeats.timer.OrderRoomTimer;

@Controller
public class OrderController {

    @Autowired
    DeliveryAddressService deliveryAddressService;
	
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

    @Autowired
    TransactionService transactionService;

    @Autowired
    StoreMapper storeMapper;
    
    @GetMapping("/orders/{order_idx}")
    public String orderDetail(
            Model model,
            @PathVariable("order_idx") int orderIdx,
            @RequestAttribute("groupOrder") GroupOrder groupOrder,
            @RequestAttribute("payment") Payment payment) {

        if (!"PAID".equals(payment.getPaymentStatus())) {
            return String.format("redirect:/orders/%d/payment", orderIdx);
        }

        final record MemberItems(RoomParticipant roomParticipant, Member member, List<GroupOrderItem> items) {}

        Map<Integer, List<GroupOrderItem>> groupOrderItems = groupOrderService.findByOrder(groupOrder.getOrderIdx())
                .stream()
                .collect(Collectors.groupingBy(GroupOrderItem::getMemberIdx));

        Map<Integer, RoomParticipant> roomParticipantMap = orderRoomService.findByRoom(groupOrder.getRoomIdx())
                .stream()
                .collect(Collectors.toMap(RoomParticipant::getMemberIdx, roomParticipant -> roomParticipant));

        List<MemberItems> memberItems = memberService.findByIdxs(roomParticipantMap.keySet())
                .stream()
                .map(roomMember -> new MemberItems(
                        roomParticipantMap.get(roomMember.getMemberIdx()),
                        roomMember,
                        groupOrderItems.getOrDefault(roomMember.getMemberIdx(), List.of())))
                .toList();

        model.addAttribute("orderRoom", orderRoomService.findByIdx(groupOrder.getRoomIdx()));
        model.addAttribute("orderIdx", orderIdx);
        model.addAttribute("groupOrder", groupOrder);
        model.addAttribute("payment", payment);
        model.addAttribute("memberItems", memberItems);
        model.addAttribute("hideFloatingOrderStatusButton", true);

        return "order-detail";
    }
    
    public record OrderSummaryResponse(
            String orderStatus,
            int currentStep,
            String currentStepLabel,
            String orderNumber,
            String storeName,
            Integer orderAmount,
            String deliveryLabel,
            String deliveryAddress
    ) {}

    private int resolveOrderStep(String orderStatus) {
        return switch (orderStatus) {
            case "PAID" -> 1;
            case "ACCEPTED" -> 2;
            case "PREPARING" -> 3;
            case "READY", "DELIVERING" -> 4;
            case "COMPLETED" -> 5;
            default -> 0;
        };
    }

    private String resolveOrderStepLabel(String orderStatus) {
        return switch (orderStatus) {
            case "PAYMENT_PENDING" -> "결제 대기";
            case "PAID" -> "결제 완료";
            case "ACCEPTED" -> "주문 확인";
            case "PREPARING" -> "준비 중";
            case "READY", "DELIVERING" -> "배달 시작";
            case "COMPLETED" -> "배달 완료";
            case "CANCELLED" -> "주문 취소";
            default -> orderStatus;
        };
    }

    @GetMapping("/orders/{order_idx}/summary")
    @ResponseBody
    public OrderSummaryResponse orderSummary(
            @PathVariable("order_idx") int orderIdx,
            @RequestAttribute("groupOrder") GroupOrder groupOrder,
            @SessionAttribute("member") Member member) {

        GroupOrderService.GroupOrderRecord record = groupOrderService.findRecordByIdx(orderIdx);
        var orderRoom = orderRoomService.findByIdx(groupOrder.getRoomIdx());

        String orderNumber = String.valueOf(orderIdx);
        if (orderRoom != null
                && orderRoom.getRoomCode() != null
                && !orderRoom.getRoomCode().isBlank()) {
            orderNumber = orderRoom.getRoomCode();
        }

        String storeName = "가게 정보 없음";
        if (groupOrder.getStoreIdx() > 0) {
            var store = storeMapper.findByStoreIdx(groupOrder.getStoreIdx());
            if (store != null
                    && store.getStoreName() != null
                    && !store.getStoreName().isBlank()) {
                storeName = store.getStoreName();
            }
        }

        String deliveryLabel = "배송지";
        String deliveryAddress = "주소 정보 없음";

        if (record != null && record.orderDelivery() != null) {
            int sourceDeliveryAddressIdx = record.orderDelivery().getSourceDeliveryAddressIdx();

            if (sourceDeliveryAddressIdx > 0) {
                var sourceAddress = deliveryAddressService.addressByIdx(member.getMemberIdx(), sourceDeliveryAddressIdx);
                if (sourceAddress != null
                        && sourceAddress.getDeliveryLabel() != null
                        && !sourceAddress.getDeliveryLabel().isBlank()) {
                    deliveryLabel = sourceAddress.getDeliveryLabel();
                }
            }

            String address1 = record.orderDelivery().getDeliveryAddress1();
            String address2 = record.orderDelivery().getDeliveryAddress2();

            deliveryAddress = (address1 != null ? address1 : "")
                    + ((address2 != null && !address2.isBlank()) ? " " + address2 : "");

            if (deliveryAddress.isBlank()) {
                deliveryAddress = "주소 정보 없음";
            }
        }

        return new OrderSummaryResponse(
                groupOrder.getOrderStatus(),
                resolveOrderStep(groupOrder.getOrderStatus()),
                resolveOrderStepLabel(groupOrder.getOrderStatus()),
                orderNumber,
                storeName,
                groupOrder.getOrderTotalAmount(),
                deliveryLabel,
                deliveryAddress
        );
    }
    

    @GetMapping("/orders/{order_idx}/payment")
    public String orderPayment(
            RedirectAttributes ra,
            @PathVariable("order_idx") int orderIdx,
            @RequestAttribute("groupOrder") GroupOrder groupOrder,
            @RequestAttribute("payment") Payment payment,
            @SessionAttribute("member") Member member) {

        PaymentShare paymentShare = paymentService.findPaymentMember(payment.getPaymentIdx(), member.getMemberIdx());

        if (paymentShare == null) {
            ra.addFlashAttribute("error", "오류가 발생했습니다 관리자에게 연락해주십시오");
            return "redirect:/main";
        }

        if ("PAID".equals(payment.getPaymentStatus())) {
            return String.format("redirect:/orders/%d", orderIdx);
        }

        String destination;

        if ("REPRESENTATIVE".equals(groupOrder.getPaymentMode())) {
            if (groupOrder.getLeaderMemberIdx() == member.getMemberIdx()
                    && !"PAID_SELF".equals(paymentShare.getShareStatus())) {
                destination = "representative";
            } else {
                destination = "wait";
            }
        } else {
            if ("PAID_SELF".equals(paymentShare.getShareStatus())) {
                destination = "wait";
            } else {
                destination = "individual";
            }
        }

        return String.format("redirect:/orders/%d/payment/%s", orderIdx, destination);
    }

    @GetMapping("/orders/{order_idx}/payment/representative")
    public String representativePay(
            Model model,
            @PathVariable("order_idx") int orderIdx,
            @RequestAttribute("groupOrder") GroupOrder groupOrder,
            @RequestAttribute("payment") Payment payment,
            @RequestAttribute("paymentShare") PaymentShare paymentShare,
            @SessionAttribute("member") Member member) {

        if (member.getMemberIdx() != groupOrder.getLeaderMemberIdx()) {
            return String.format("redirect:/orders/%d/payment/wait", orderIdx);
        }

        if ("PAID_SELF".equals(paymentShare.getShareStatus()) || "PAID".equals(payment.getPaymentStatus())) {
            return String.format("redirect:/orders/%d", orderIdx);
        }

        List<PaymentShare> paymentShares = paymentService.findByPayment(payment.getPaymentIdx());
        bindPaymentModel(model, orderIdx, groupOrder, payment, paymentShare, paymentShares);

        return "payment-representative";
    }

    @GetMapping("/orders/{order_idx}/payment/individual")
    public String individualPay(
            RedirectAttributes ra,
            Model model,
            @PathVariable("order_idx") int orderIdx,
            @RequestAttribute("groupOrder") GroupOrder groupOrder,
            @RequestAttribute("payment") Payment payment,
            @RequestAttribute("paymentShare") PaymentShare paymentShare,
            @SessionAttribute("member") Member member) {

        // 💡 [수정] 1. 이미 결제가 끝났는지 가장 먼저 검사합니다.
        if ("PAID".equals(payment.getPaymentStatus()) || "PAID_SELF".equals(paymentShare.getShareStatus())) {
            return String.format("redirect:/orders/%d/payment/wait", orderIdx);
        }

        // 💡 [수정] 2. 결제가 진행 중인데 방이 없다면 진짜 폭파된 것입니다.
        if (orderRoomService.findActiveRoomByMember(member.getMemberIdx()) == null) {
            ra.addFlashAttribute("error", "주문방이 폭파되었거나 결제 시간이 만료되었습니다.");
            return "redirect:/main";
        }

        if ("CANCELLED".equals(payment.getPaymentStatus()) || "CANCELLED".equals(paymentShare.getShareStatus())) {
            return String.format("redirect:/orders/%d", orderIdx);
        }

        List<PaymentShare> paymentShares = paymentService.findByPayment(payment.getPaymentIdx());
        bindPaymentModel(model, orderIdx, groupOrder, payment, paymentShare, paymentShares);

        return "payment-individual";
    }

    @GetMapping("/orders/{order_idx}/payment/wait")
    public String waitForPayment(
            RedirectAttributes ra,
            Model model,
            @PathVariable("order_idx") int orderIdx,
            @RequestAttribute("groupOrder") GroupOrder groupOrder,
            @RequestAttribute("payment") Payment payment,
            @RequestAttribute("paymentShare") PaymentShare paymentShare,
            @SessionAttribute("member") Member member) {

        // 💡 [수정] 1. 전원 결제가 완료된 경우 방 검사를 무시하고 최우선으로 주문 상세로 보냅니다.
        if ("PAID".equals(payment.getPaymentStatus())) {
            return String.format("redirect:/orders/%d", orderIdx);
        }

        // 💡 [수정] 2. 전원 결제가 완료되지 않았는데 방이 없다면 폭파된 것입니다.
        if (orderRoomService.findActiveRoomByMember(member.getMemberIdx()) == null) {
            ra.addFlashAttribute("error", "주문방이 폭파되었거나 결제 시간이 만료되었습니다.");
            return "redirect:/main";
        }

        if ("CANCELLED".equals(payment.getPaymentStatus()) || "CANCELLED".equals(paymentShare.getShareStatus())) {
            return String.format("redirect:/orders/%d", orderIdx);
        }

        if ("INDIVIDUAL".equals(payment.getPaymentMode()) && "PENDING".equals(paymentShare.getShareStatus())) {
            return String.format("redirect:/orders/%d/payment", orderIdx);
        }

        List<PaymentShare> paymentShares = paymentService.findByPayment(payment.getPaymentIdx());
        bindPaymentModel(model, orderIdx, groupOrder, payment, paymentShare, paymentShares);
        model.addAttribute("isLeader", groupOrder.getLeaderMemberIdx() == member.getMemberIdx());

        return "payment-wait";
    }

    @PostMapping("/orders/{order_idx}/payment/complete")
    public String paymentComplete(
            RedirectAttributes ra,
            @PathVariable("order_idx") int orderIdx,
            @RequestAttribute("groupOrder") GroupOrder groupOrder,
            @RequestAttribute("payment") Payment payment,
            @RequestAttribute("paymentShare") PaymentShare paymentShare,
            @SessionAttribute("member") Member member) {

        int paymentShareIdx = paymentShare.getPaymentShareIdx();

        if ("REPRESENTATIVE".equals(payment.getPaymentMode())
                && member.getMemberIdx() != groupOrder.getLeaderMemberIdx()) {
            ra.addFlashAttribute("error", "잘못된 접근입니다");
            return "redirect:/main";
        }

        if ("PAID".equals(payment.getPaymentStatus())) {
            return String.format("redirect:/orders/%d", orderIdx);
        }

        if (!"PENDING".equals(paymentShare.getShareStatus())) {
            return "INDIVIDUAL".equals(payment.getPaymentMode())
                    ? String.format("redirect:/orders/%d/payment/wait", orderIdx)
                    : String.format("redirect:/orders/%d", orderIdx);
        }

        if (!transactionService.completePayment(groupOrder, payment, paymentShare)) {
            Payment currentPayment = paymentService.findByOrder(orderIdx);
            PaymentShare currentShare = paymentService.findShareByIdx(paymentShareIdx);
            if (currentPayment != null && "PAID".equals(currentPayment.getPaymentStatus())) {
                return String.format("redirect:/orders/%d", orderIdx);
            }
            if (currentShare != null && !"PENDING".equals(currentShare.getShareStatus())) {
                return "INDIVIDUAL".equals(payment.getPaymentMode())
                        ? String.format("redirect:/orders/%d/payment/wait", orderIdx)
                        : String.format("redirect:/orders/%d", orderIdx);
            }
            ra.addFlashAttribute("error", "결제 중 오류가 발생했습니다 관리자에게 연락해주십시오");
            return String.format("redirect:/orders/%d/payment", orderIdx);
        }

        if ("REPRESENTATIVE".equals(payment.getPaymentMode())) {
            orderRoomTimer.stop(orderIdx);
            sseService.payOrder(orderIdx, paymentShareIdx);
            sseService.payComplete(orderIdx, groupOrder.getStoreIdx());

            return String.format("redirect:/orders/%d", orderIdx);
        }

        sseService.payOrder(orderIdx, paymentShareIdx);

        if (paymentService.findPaymentPending(payment.getPaymentIdx()).isEmpty()) {
            orderRoomTimer.stop(orderIdx);
            sseService.payComplete(orderIdx, groupOrder.getStoreIdx());
            return String.format("redirect:/orders/%d", orderIdx);
        }

        return String.format("redirect:/orders/%d/payment/wait", orderIdx);
    }

    private void bindPaymentModel(
            Model model,
            int orderIdx,
            GroupOrder groupOrder,
            Payment payment,
            PaymentShare paymentShare,
            List<PaymentShare> paymentShares) {

        long completedCount = paymentShares.stream()
                .filter(share -> "PAID_SELF".equals(share.getShareStatus())
                        || "PAID_BY_REPRESENTATIVE".equals(share.getShareStatus()))
                .count();

        model.addAttribute("orderIdx", orderIdx);
        model.addAttribute("groupOrder", groupOrder);
        model.addAttribute("payment", payment);
        model.addAttribute("paymentShare", paymentShare);
        model.addAttribute("paymentShares", paymentShares);
        model.addAttribute("memberNameMap", memberService.findByIdxs(
                paymentShares.stream().map(PaymentShare::getMemberIdx).toList()).stream()
                .collect(Collectors.toMap(Member::getMemberIdx, Member::getMemberNickname)));
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalCount", paymentShares.size());
    }
    
    @PostMapping("/orders/{order_idx}/payment/cancel")
    public String cancelPayment(
            RedirectAttributes ra,
            @PathVariable("order_idx") int orderIdx,
            @RequestAttribute("groupOrder") GroupOrder groupOrder,
            @SessionAttribute("member") Member member) {

        if (groupOrder.getLeaderMemberIdx() != member.getMemberIdx()) {
            ra.addFlashAttribute("error", "잘못된 접근입니다");
            return "redirect:/main";
        }

        if (orderRoomService.cancel(groupOrder.getRoomIdx()) == 0) {
            ra.addFlashAttribute("error", "결제 취소 및 방 폭파 중 오류가 발생했습니다");
            return String.format("redirect:/orders/%d/payment", orderIdx);
        }

        orderRoomTimer.stop(orderIdx);
        sseService.cancelRoom(groupOrder.getRoomIdx());

        // ra.addFlashAttribute("message", "결제를 취소하여 주문방이 폭파되었습니다.");
        return "redirect:/main";
    }
}