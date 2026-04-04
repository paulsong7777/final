package com.moeats.intercepter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.moeats.domain.Member;
import com.moeats.domain.Payment;
import com.moeats.domain.PaymentShare;
import com.moeats.services.GroupOrderService;
import com.moeats.services.OrderRoomService;
import com.moeats.services.PaymentService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PaymentIntercepter implements HandlerInterceptor {
	@Autowired
	GroupOrderService groupOrderService;
	@Autowired
	OrderRoomService orderRoomService;
	@Autowired
	PaymentService paymentService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 가게 주인이 방 참여자일 경우도 있을 수 있지만 이 경우는 고려하지 않음
//		if((boolean)request.getAttribute("isOwner")) {
//			FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
//			FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
//			flashMap.put("error", "잘못된 접근입니다");
//			flashMapManager.saveOutputFlashMap(flashMap, request, response);
//			response.sendRedirect(String.format("/home"));
//			return false;
//		}

		Payment payment = (Payment) request.getAttribute("payment");
        Member member = (Member) request.getSession().getAttribute("member");
		PaymentShare paymentShare = paymentService.findPaymentMember(payment.getPaymentIdx(), member.getMemberIdx());
		request.setAttribute("paymentShare", paymentShare);
		return true;
	}
}