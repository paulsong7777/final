package com.moeats.config; // 패키지명은 프로젝트 구조에 맞게 수정하세요

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.moeats.interceptor.LoginCheckInterceptor;

import com.moeats.interceptor.OrderMemberInterceptor;
import com.moeats.interceptor.OwnerCheckInterceptor;
import com.moeats.interceptor.PaymentInterceptor;
import com.moeats.interceptor.RoomMemberInterceptor;


@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        
        registry.addInterceptor(loginCheckInterceptor)
                // 🛑 1. 문지기가 지킬(로그인이 필요한) URL 주소들을 지정합니다.
                .addPathPatterns(
                        "/member/me/**",     // 마이페이지 관련 전부
                        "/members/me/**",    // 배송지 관리 등 전부
                        "/owner/**"          // 사장님 기능 전부 (메뉴 사진, 가게 관리 등)
                )
                // 🟢 2. 예외적으로 문지기가 그냥 통과시켜줄 URL 주소들을 지정합니다. (필요시)
                .excludePathPatterns(
                        "/login", 
                        "/members/new", 
                        "/css/**", "/js/**", "/images/**" // 정적 리소스는 무조건 통과!
                );
    }
}