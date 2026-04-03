package com.moeats.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.moeats.intercepter.OrderMemberIntercepter;
import com.moeats.intercepter.PaymentIntercepter;
import com.moeats.intercepter.RoomMemberIntercepter;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
	
	//================All Previous Interceptors======================
	
	@Autowired
	RoomMemberIntercepter roomMemberIntercepter;
	@Autowired
	OrderMemberIntercepter orderMemberIntercepter;
	@Autowired
	PaymentIntercepter paymentIntercepter;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		
		//================All Previous Interceptors======================
		
		registry.addInterceptor(roomMemberIntercepter)
				.addPathPatterns("/rooms/code/{room_code}/**")
				.excludePathPatterns("/rooms/code/{room_code}");
		
		registry.addInterceptor(orderMemberIntercepter)
				.addPathPatterns("/orders/{order_idx}/**");
		
		registry.addInterceptor(paymentIntercepter)
				.addPathPatterns("/orders/{order_idx}/payment/**");
	}
}
