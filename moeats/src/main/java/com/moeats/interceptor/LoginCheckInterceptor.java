package com.moeats.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

	    if (request.getDispatcherType() == DispatcherType.ERROR) {
	        return true;
	    }

	    HttpSession session = request.getSession(false);

	    if (session == null || session.getAttribute("member") == null) {

	        if (response.isCommitted()) {
	            return false;
	        }

	        String requestURI = request.getRequestURI();
	        String queryString = request.getQueryString();
	        String redirectURI = requestURI + (queryString != null ? "?" + queryString : "");

	        HttpSession newSession = request.getSession(true);
	        newSession.setAttribute("redirectURI", redirectURI);

	        response.sendRedirect("/login");
	        return false;
	    }

	    return true;
	}
}