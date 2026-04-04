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
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession();
        Member member = (Member) session.getAttribute("member");

        // 1. 로그인 안된 경우
        if (member == null) {
            response.sendRedirect("/login");
            return false;
        }

        // 2. OWNER가 아닌 경우
        if (!"OWNER".equals(member.getMemberRoleType())) {
            response.sendRedirect("/main");
            return false;
        }

        return true;
    }
}