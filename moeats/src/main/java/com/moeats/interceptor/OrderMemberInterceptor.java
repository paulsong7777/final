package com.moeats.interceptor;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.moeats.domain.GroupOrder;
import com.moeats.domain.Member;
import com.moeats.domain.Payment;
import com.moeats.services.GroupOrderService;
import com.moeats.services.OrderRoomService;
import com.moeats.services.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OrderMemberInterceptor implements HandlerInterceptor {

    @Autowired
    private GroupOrderService groupOrderService;

    @Autowired
    private OrderRoomService orderRoomService;

    @Autowired
    private PaymentService paymentService;

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Map<String, String> pathParams =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        HttpSession session = request.getSession(false);
        Member member = session != null ? (Member) session.getAttribute("member") : null;

        if (member == null) {
            response.sendRedirect("/login");
            return false;
        }

        if (pathParams == null || pathParams.get("order_idx") == null) {
            response.sendRedirect("/main");
            return false;
        }

        try {
            int orderIdx = Integer.parseInt(pathParams.get("order_idx"));
            GroupOrder groupOrder = groupOrderService.findByIdx(orderIdx);
            Payment payment = paymentService.findByOrder(orderIdx);

            if (groupOrder == null
                    || payment == null
                    || orderRoomService.findRoomMember(groupOrder.getRoomIdx(), member.getMemberIdx()) == null) {
                return deny(request, response);
            }

            request.setAttribute("groupOrder", groupOrder);
            request.setAttribute("payment", payment);
            return true;

        } catch (NumberFormatException e) {
            return deny(request, response);
        }
    }

    private boolean deny(HttpServletRequest request, HttpServletResponse response) throws Exception {
        FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
        FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
        flashMap.put("error", "잘못된 접근입니다");
        flashMapManager.saveOutputFlashMap(flashMap, request, response);
        response.sendRedirect("/main");
        return false;
    }
}
