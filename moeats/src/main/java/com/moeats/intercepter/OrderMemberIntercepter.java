package com.moeats.intercepter;

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
public class OrderMemberIntercepter implements HandlerInterceptor {
	@Autowired
	GroupOrderService groupOrderService;
	@Autowired
	OrderRoomService orderRoomService;
	@Autowired
	PaymentService paymentService;
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		Map<String, String> pathParams = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        HttpSession session = request.getSession();
        Member member = (Member) session.getAttribute("member");
		if(pathParams!=null) {
			try {
				int orderIdx = Integer.parseInt(pathParams.get("order_idx"));
				GroupOrder groupOrder = groupOrderService.findByIdx(orderIdx);
				Payment payment = paymentService.findByOrder(orderIdx);
//				Store store = storeService.findByIdx(groupOrder.getStoreIdx());
//				boolean isOwner = store.getOwnerMemberIdx()==member.getMemberIdx();
				// 해당 가게 주인이거나 방 참여자
				assert payment!=null;
				assert /*isOwner ||*/ orderRoomService.findRoomMember(groupOrder.getRoomIdx(),member.getMemberIdx())!=null;
				request.setAttribute("groupOrder", groupOrder);
				request.setAttribute("payment", payment);
//				request.setAttribute("isOwner", isOwner);
			}catch(NumberFormatException | NullPointerException | AssertionError e) {
				FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
				FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
				flashMap.put("error", "잘못된 접근입니다");
				flashMapManager.saveOutputFlashMap(flashMap, request, response);
				response.sendRedirect("/main");
				return false;
			}
		}
		return true;
	}
}