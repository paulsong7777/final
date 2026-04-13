package com.moeats.interceptor;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
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
    
    private boolean forRestAPI(HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        boolean isRest =
                handlerMethod.getBeanType().isAnnotationPresent(RestController.class)
                || handlerMethod.hasMethodAnnotation(ResponseBody.class);

        if (!isRest) {
            return false;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        try {
            response.getWriter().write("{\"result\": false}");
        } catch (IOException e) {
        }
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Map<String, String> pathParams =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        HttpSession session = request.getSession(false);
        Member member = session != null ? (Member) session.getAttribute("member") : null;
        if (member == null) {
        	if(!forRestAPI(response, handler))
        		response.sendRedirect("/login");
            return false;
        }

        if (pathParams == null) {
        	if(!forRestAPI(response, handler))
        		response.sendRedirect("/main");
            return false;
        }

        String roomCode = pathParams.get("room_code");
        if (roomCode == null || roomCode.isBlank()) {
        	if(!forRestAPI(response, handler))
        		response.sendRedirect("/main");
            return false;
        }

        OrderRoom orderRoom = orderRoomService.findByCode(roomCode);
        if (orderRoom == null || orderRoomService.findJoinedRoomMember(orderRoom.getRoomIdx(), member.getMemberIdx()) == null) {
        	if(!forRestAPI(response, handler)) {
	            FlashMap flashMap = RequestContextUtils.getOutputFlashMap(request);
	            FlashMapManager flashMapManager = RequestContextUtils.getFlashMapManager(request);
	            flashMap.put("error", "방 접근 권한이 없습니다.");
	            flashMapManager.saveOutputFlashMap(flashMap, request, response);
	            response.sendRedirect("/rooms/join");
        	}
            return false;
        }

        request.setAttribute("orderRoom", orderRoom);
        return true;
    }
}
