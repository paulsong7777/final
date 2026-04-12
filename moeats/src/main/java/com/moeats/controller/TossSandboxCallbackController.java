package com.moeats.controller;

import com.moeats.domain.GroupOrder;
import com.moeats.domain.Payment;
import com.moeats.domain.PaymentTransaction;
import com.moeats.domain.TossPaymentResponse;
import com.moeats.mapper.PaymentTransactionMapper;
import com.moeats.service.TossPaymentCallbackService;
import com.moeats.services.GroupOrderService;
import com.moeats.services.PaymentService;
import com.moeats.services.sse.SSEService;
import com.moeats.timer.OrderRoomTimer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class TossSandboxCallbackController {

    private final TossPaymentCallbackService tossPaymentCallbackService;
    private final PaymentTransactionMapper paymentTransactionMapper;
    private final PaymentService paymentService;
    private final GroupOrderService groupOrderService;
    private final SSEService sseService;
    private final OrderRoomTimer orderRoomTimer;

    public TossSandboxCallbackController(
            TossPaymentCallbackService tossPaymentCallbackService,
            PaymentTransactionMapper paymentTransactionMapper,
            PaymentService paymentService,
            GroupOrderService groupOrderService,
            SSEService sseService,
            OrderRoomTimer orderRoomTimer
    ) {
        this.tossPaymentCallbackService = tossPaymentCallbackService;
        this.paymentTransactionMapper = paymentTransactionMapper;
        this.paymentService = paymentService;
        this.groupOrderService = groupOrderService;
        this.sseService = sseService;
        this.orderRoomTimer = orderRoomTimer;
    }

    @GetMapping("/sandbox/toss/success")
    public String success(@RequestParam("paymentKey") String paymentKey,
                          @RequestParam("orderId") String orderId,
                          @RequestParam("amount") Integer amount,
                          RedirectAttributes ra) {

        TossPaymentResponse response = tossPaymentCallbackService.confirm(paymentKey, orderId, amount);

        PaymentTransaction transaction = paymentTransactionMapper.selectByMerchantOrderId(orderId);
        if (transaction == null || transaction.getPaymentIdx() == null) {
            ra.addFlashAttribute("error", "결제 승인 후 주문 정보를 찾지 못했습니다.");
            return "redirect:/main";
        }

        Payment payment = paymentService.findByIdx(transaction.getPaymentIdx().intValue());
        if (payment == null) {
            ra.addFlashAttribute("error", "결제 승인 후 결제 정보를 찾지 못했습니다.");
            return "redirect:/main";
        }

        GroupOrder groupOrder = groupOrderService.findByIdx(payment.getOrderIdx());
        if (groupOrder == null) {
            ra.addFlashAttribute("error", "결제 승인 후 주문 정보를 찾지 못했습니다.");
            return "redirect:/main";
        }

        int orderIdx = groupOrder.getOrderIdx();

        if ("REPRESENTATIVE".equals(transaction.getTransactionType())) {
            orderRoomTimer.stop(orderIdx);
            sseService.payComplete(orderIdx, groupOrder.getStoreIdx());
            return "redirect:/orders/" + orderIdx;
        }

        if (transaction.getPaymentShareIdx() != null) {
            sseService.payOrder(orderIdx, transaction.getPaymentShareIdx().intValue());
        }

        if ("PAID".equals(payment.getPaymentStatus())) {
            orderRoomTimer.stop(orderIdx);
            sseService.payComplete(orderIdx, groupOrder.getStoreIdx());
            return "redirect:/orders/" + orderIdx;
        }

        return "redirect:/orders/" + orderIdx + "/payment/wait";
    }

    @GetMapping("/sandbox/toss/fail")
    public String fail(@RequestParam(value = "code", required = false) String code,
                       @RequestParam(value = "message", required = false) String message,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       RedirectAttributes ra) {

        tossPaymentCallbackService.handleFail(orderId, code, message);

        if (orderId == null || orderId.isBlank()) {
            ra.addFlashAttribute("error", "결제에 실패했습니다.");
            return "redirect:/main";
        }

        PaymentTransaction transaction = paymentTransactionMapper.selectByMerchantOrderId(orderId);
        if (transaction == null || transaction.getPaymentIdx() == null) {
            ra.addFlashAttribute("error", "결제에 실패했습니다.");
            return "redirect:/main";
        }

        Payment payment = paymentService.findByIdx(transaction.getPaymentIdx().intValue());
        if (payment == null) {
            ra.addFlashAttribute("error", "결제에 실패했습니다.");
            return "redirect:/main";
        }

        ra.addFlashAttribute("error", "결제에 실패했습니다. " + (message != null ? message : ""));
        return "redirect:/orders/" + payment.getOrderIdx() + "/payment";
    }
}