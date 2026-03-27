package com.moeats.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("member") == null) {
            
            // 💡 추가된 부분: 쫓아내기 전에 사용자가 원래 가려고 했던 주소를 파악합니다.
            String requestURI = request.getRequestURI(); 
            String queryString = request.getQueryString(); // ?page=2 같은 파라미터도 챙겨줍니다.
            String redirectURI = requestURI + (queryString != null ? "?" + queryString : "");

            // 세션이 아예 죽어있을 수 있으니 새로 하나 만들어서(true) 메모를 남깁니다.
            HttpSession newSession = request.getSession(true);
            newSession.setAttribute("redirectURI", redirectURI); // "redirectURI"라는 이름표로 저장!
            
            // 로그인 페이지로 튕겨냅니다.
            response.sendRedirect("/login");
            return false; 
        }
        
        return true; 
    }
}