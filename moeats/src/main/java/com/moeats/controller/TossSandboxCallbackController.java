package com.moeats.controller;

import com.moeats.domain.TossPaymentResponse;
import com.moeats.service.TossPaymentCallbackService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TossSandboxCallbackController {

    private final TossPaymentCallbackService tossPaymentCallbackService;

    public TossSandboxCallbackController(TossPaymentCallbackService tossPaymentCallbackService) {
        this.tossPaymentCallbackService = tossPaymentCallbackService;
    }

    @GetMapping("/sandbox/toss/success")
    public String success(@RequestParam("paymentKey") String paymentKey,
                          @RequestParam("orderId") String orderId,
                          @RequestParam("amount") Integer amount,
                          Model model) {
        TossPaymentResponse response = tossPaymentCallbackService.confirm(paymentKey, orderId, amount);

        model.addAttribute("paymentKey", response.getPaymentKey());
        model.addAttribute("orderId", response.getOrderId());
        model.addAttribute("method", response.getMethod());
        model.addAttribute("status", response.getStatus());
        model.addAttribute("amount", response.getTotalAmount());

        return "home/toss-success";
    }

    @GetMapping("/sandbox/toss/fail")
    public String fail(@RequestParam(value = "code", required = false) String code,
                       @RequestParam(value = "message", required = false) String message,
                       @RequestParam(value = "orderId", required = false) String orderId,
                       Model model) {
        tossPaymentCallbackService.handleFail(orderId, code, message);

        model.addAttribute("code", code);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);

        return "home/toss-fail";
    }
}
