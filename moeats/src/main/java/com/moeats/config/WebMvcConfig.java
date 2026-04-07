package com.moeats.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.moeats.interceptor.LoginCheckInterceptor;
import com.moeats.interceptor.OwnerCheckInterceptor;
import com.moeats.interceptor.OrderMemberInterceptor;
import com.moeats.interceptor.PaymentInterceptor;
import com.moeats.interceptor.RoomMemberInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;

    @Autowired
    private OwnerCheckInterceptor ownerCheckInterceptor;

    @Autowired
    private RoomMemberInterceptor roomMemberInterceptor;

    @Autowired
    private OrderMemberInterceptor orderMemberInterceptor;

    @Autowired
    private PaymentInterceptor paymentInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // 로그인만 필요한 경로
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns(
                        "/members/me/**",
                        "/members/dashboard",
                        "/rooms/**",
                        "/orders/**"
                )
                .excludePathPatterns(
                        "/login",
                        "/css/**",
                        "/js/**",
                        "/images/**"
                );

        // 점주 전용 경로 - 먼저 로그인 체크
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns("/owners/**")
                .excludePathPatterns(
                        "/login",
                        "/css/**",
                        "/js/**",
                        "/images/**"
                );

        // 점주 전용 경로 - OWNER 권한 체크
        registry.addInterceptor(ownerCheckInterceptor)
                .addPathPatterns("/owners/**");

        registry.addInterceptor(roomMemberInterceptor)
                .addPathPatterns(
                        "/rooms/code/*/leave",
                        "/rooms/code/*/kick",
                        "/rooms/code/*/cancel",
                        "/rooms/code/*/cart",
                        "/rooms/code/*/cart/**",
                        "/rooms/code/*/checkout"
                );

        registry.addInterceptor(orderMemberInterceptor)
                .addPathPatterns(
                        "/orders/*",
                        "/orders/*/payment",
                        "/orders/*/payment/**"
                );

        registry.addInterceptor(paymentInterceptor)
                .addPathPatterns(
                        "/orders/*/payment/**"
                );
    }
}
