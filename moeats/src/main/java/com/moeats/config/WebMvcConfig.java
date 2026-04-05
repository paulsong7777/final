package com.moeats.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.moeats.interceptor.LoginCheckInterceptor;
import com.moeats.intercepter.OrderMemberIntercepter;
import com.moeats.intercepter.PaymentIntercepter;
import com.moeats.intercepter.RoomMemberIntercepter;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginCheckInterceptor loginCheckInterceptor;

    @Autowired
    private RoomMemberIntercepter roomMemberIntercepter;

    @Autowired
    private OrderMemberIntercepter orderMemberIntercepter;

    @Autowired
    private PaymentIntercepter paymentIntercepter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns(
                        "/members/me/**",
                        "/members/dashboard",
                        "/owners/**",
                        "/rooms/**",
                        "/orders/**"
                )
                .excludePathPatterns(
                        "/login",
                        "/css/**",
                        "/js/**",
                        "/images/**"
                );

        registry.addInterceptor(roomMemberIntercepter)
                .addPathPatterns(
                        "/rooms/code/*/leave",
                        "/rooms/code/*/kick",
                        "/rooms/code/*/cancel",
                        "/rooms/code/*/cart",
                        "/rooms/code/*/cart/**",
                        "/rooms/code/*/checkout"
                );

        registry.addInterceptor(orderMemberIntercepter)
                .addPathPatterns(
                        "/orders/*",
                        "/orders/*/payment",
                        "/orders/*/payment/**"
                );

        registry.addInterceptor(paymentIntercepter)
                .addPathPatterns(
                        "/orders/*/payment/**"
                );
    }
}