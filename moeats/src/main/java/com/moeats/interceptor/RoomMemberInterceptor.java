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
import com.moeats.domain.OrderRoom;
import com.moeats.services.OrderRoomService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class RoomMemberInterceptor implements HandlerInterceptor {

    @Autowired
    private OrderRoomService orderRoomService;

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

        if (pathParams == null) {
            response.sendRedirect("/main");
            return false;
        }

        String roomCode = pathParams.get("room_code");
        if (roomCode == null || roomCode.isBlank()) {
            response.sendRedirect("/main");
            return false;
        }

        OrderRoom orderRoom = orderRoomService.findByCode(roomCode);
        if (orderRoom == null || orderRoomService.findRoomMember(orderRoom.getRoomIdx(), member.getMemberIdx()) == null) {
            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
            FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
            flashMap.put("error", "?섎せ???묎렐?낅땲??);
            flashMapManager.saveOutputFlashMap(flashMap, request, response);
            response.sendRedirect("/rooms/join?roomCode=" + roomCode);
            return false;
        }

        request.setAttribute("orderRoom", orderRoom);
        return true;
    }
}
