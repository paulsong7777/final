package com.moeats.interceptor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.moeats.domain.Member;
import com.moeats.domain.Payment;
import com.moeats.domain.PaymentShare;
import com.moeats.services.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class PaymentInterceptor implements HandlerInterceptor {

    @Autowired
    private PaymentService paymentService;

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Payment payment = (Payment) request.getAttribute("payment");

        HttpSession session = request.getSession(false);
        Member member = session != null ? (Member) session.getAttribute("member") : null;

        if (payment == null || member == null) {
            response.sendRedirect("/login");
            return false;
        }

        PaymentShare paymentShare = paymentService.findPaymentMember(payment.getPaymentIdx(), member.getMemberIdx());
        if (paymentShare == null) {
            Map<String, String> pathParams =
                    (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
            flashMap.put("error", "?섎せ???묎렐?낅땲??);
            flashMapManager.saveOutputFlashMap(flashMap, request, response);

            String orderIdx = pathParams != null ? pathParams.get("order_idx") : null;
            response.sendRedirect(orderIdx != null ? "/orders/" + orderIdx : "/main");
            return false;
        }

        request.setAttribute("paymentShare", paymentShare);
        return true;
    }
}
