package com.moeats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeControllerSample {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("activeNav", "home");
        model.addAttribute("userRole", "USER");
        model.addAttribute("loginUser", null);
        model.addAttribute("storeList", List.of(
                new StoreCard(1L, "교촌 반월당점", "치킨", "단체 주문에 적합한 대표 인기 메뉴 운영", "15,000원", "배달 가능 / 현장 가능", "20~30분", "https://images.unsplash.com/photo-1513104890138-7c749659a591?auto=format&fit=crop&w=1200&q=80"),
                new StoreCard(2L, "버거하우스 동성로점", "버거", "세트 주문 구성이 빠르고 공동 주문에 익숙한 매장", "18,000원", "배달 가능", "15~25분", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=1200&q=80"),
                new StoreCard(3L, "샐러드랩 중앙로점", "샐러드", "라이트한 점심 주문에 적합한 메뉴 구성", "13,000원", "현장 가능", "10~15분", "https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=1200&q=80")
        ));
        return "home/main";
    }

    public record StoreCard(
            Long storeIdx,
            String storeName,
            String categoryName,
            String storeDescription,
            String minimumOrderAmount,
            String supportText,
            String etaText,
            String imageUrl
    ) {
    }
}
