package com.moeats.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
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

        // 로그인이 필요한 경로
        registry.addInterceptor(loginCheckInterceptor)
                .addPathPatterns(
                        "/members/me/**",
                        "/members/dashboard",
                        "/rooms/**",
                        "/orders/**",
                        "/owner/**",
                        "/owners/**"
                )
                .excludePathPatterns("/upload/**"); // 이미지 경로는 체크에서 제외!
        
        // 점주 전용 경로 - OWNER 권한 체크
        registry.addInterceptor(ownerCheckInterceptor)
                .addPathPatterns(
                		"/owner/**",
                		"/owners/**"
                )
                .excludePathPatterns("/upload/**"); // 이미지 경로는 체크에서 제외!

		registry.addInterceptor(roomMemberInterceptor)
				.addPathPatterns(
						"/rooms/code/*/**"
				)
				.excludePathPatterns(
						"/rooms/code/*"
				);

		registry.addInterceptor(orderMemberInterceptor)
				.addPathPatterns(
						"/orders/**"
				);

		registry.addInterceptor(paymentInterceptor)
				.addPathPatterns(
						"/orders/*/payment/**"
				);
	}
}
