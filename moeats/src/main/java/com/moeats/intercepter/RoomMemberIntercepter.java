package com.moeats.intercepter;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.moeats.domain.Member;
import com.moeats.domain.OrderRoom;
import com.moeats.services.OrderRoomService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class RoomMemberIntercepter implements HandlerInterceptor {
	@Autowired
	OrderRoomService orderRoomService;
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		Map<String, String> pathParams = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        HttpSession session = request.getSession();
        Member member = (Member) session.getAttribute("member");
		if(pathParams!=null) {
			String roomCode = pathParams.get("room_code");
			OrderRoom orderRoom = orderRoomService.findByCode(roomCode);
			if( orderRoom==null || orderRoomService.findRoomMember(orderRoom.getRoomIdx(),member.getMemberIdx())==null ) {
				FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
				FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
				flashMap.put("error", "잘못된 접근입니다");
				flashMapManager.saveOutputFlashMap(flashMap, request, response);
				response.sendRedirect("/rooms/join");
				return false;
			}
		}
		return true;
	}
}
