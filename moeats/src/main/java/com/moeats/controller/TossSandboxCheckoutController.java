package com.moeats.controller;

import com.moeats.service.TossPaymentWindowService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TossSandboxCheckoutController {

    private final TossPaymentWindowService tossPaymentWindowService;

    public TossSandboxCheckoutController(TossPaymentWindowService tossPaymentWindowService) {
        this.tossPaymentWindowService = tossPaymentWindowService;
    }

    @PostMapping("/sandbox/toss/checkout/representative")
    public String representative(@RequestParam("paymentIdx") Long paymentIdx) {
        String checkoutUrl = tossPaymentWindowService.openRepresentativeCheckout(paymentIdx);
        return "redirect:" + checkoutUrl;
    }

    @PostMapping("/sandbox/toss/checkout/individual")
    public String individual(@RequestParam("paymentShareIdx") Long paymentShareIdx) {
        String checkoutUrl = tossPaymentWindowService.openIndividualCheckout(paymentShareIdx);
        return "redirect:" + checkoutUrl;
    }
}
