package com.moeats.interceptor; // 패키지명은 프로젝트 구조에 맞게 수정하세요

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        // 1. 현재 요청의 세션을 가져옵니다.
        HttpSession session = request.getSession(false);
        
        // 2. 세션이 아예 없거나, 세션 안에 "member" 객체가 없다면? (비로그인 상태)
        if (session == null || session.getAttribute("member") == null) {
            
            // 3. 로그인 페이지로 튕겨냅니다.
            response.sendRedirect("/login");
            
            // 4. false를 반환하면 "컨트롤러로 더 이상 진행하지 마!" 라는 뜻입니다.
            return false; 
        }
        
        // 5. 로그인된 사용자라면 true를 반환하여 정상적으로 컨트롤러로 통과시킵니다.
        return true; 
    }
}