package com.moeats.controller;

import com.moeats.service.TossPaymentWindowService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Controller
public class TossSandboxCheckoutController {

    private final TossPaymentWindowService tossPaymentWindowService;

    public TossSandboxCheckoutController(TossPaymentWindowService tossPaymentWindowService) {
        this.tossPaymentWindowService = tossPaymentWindowService;
    }

    @PostMapping("/sandbox/toss/checkout/representative")
    public String representative(@RequestParam("paymentIdx") Long paymentIdx,
                                 HttpServletRequest request) {
        String baseUrl = resolveBaseUrl(request);
        String checkoutUrl = tossPaymentWindowService.openRepresentativeCheckout(paymentIdx, baseUrl);
        return "redirect:" + checkoutUrl;
    }

    @PostMapping("/sandbox/toss/checkout/individual")
    public String individual(@RequestParam("paymentShareIdx") Long paymentShareIdx,
                             HttpServletRequest request) {
        String baseUrl = resolveBaseUrl(request);
        String checkoutUrl = tossPaymentWindowService.openIndividualCheckout(paymentShareIdx, baseUrl);
        return "redirect:" + checkoutUrl;
    }

    private String resolveBaseUrl(HttpServletRequest request) {
        return ServletUriComponentsBuilder.fromRequestUri(request)
                .replacePath(request.getContextPath())
                .replaceQuery(null)
                .build()
                .toUriString();
    }
}