package com.moeats.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 웹 브라우저에서 /uploads/ 로 시작하는 주소를 요청하면
        registry.addResourceHandler("/uploads/**")
                // C 드라이브의 moeats_uploads 폴더에 있는 파일을 꺼내서 보여줌
                // 윈도우 경로인 경우 맨 앞에 file:/// 을 붙여야 합니다.
                .addResourceLocations("file:///C:/moeats_uploads/");
    }
}