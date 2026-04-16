package com.moeats.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.moeats.domain.Member;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class OwnerCheckInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
	    HttpSession session = request.getSession(false);

	    if (session == null) {
	        response.sendError(HttpServletResponse.SC_FORBIDDEN);
	        return false;
	    }

	    Member member = (Member) session.getAttribute("member");

	    if (member == null) {
	        response.sendError(HttpServletResponse.SC_FORBIDDEN);
	        return false;
	    }

	    if (member.getMemberRoleType() == null) {
	        response.sendError(HttpServletResponse.SC_FORBIDDEN);
	        return false;
	    }

	    if (!"OWNER".equals(member.getMemberRoleType())) {
	        response.sendError(HttpServletResponse.SC_FORBIDDEN);
	        return false;
	    }

	    return true;
	}
}