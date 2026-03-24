package com.moeats.controller.owner;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    /**
     * [GET] 점주 대시보드
     * 분기: 신규(is_empty: true) -> 등록유도 / 기존(is_empty: false) -> 지도 제어
     */
    @GetMapping("/dashboard")
    public String ownerDashboard(Model model) {
        // [로직] 실제로는 세션에서 정보를 가져와 DB 조회
        Map<String, Object> store_vo = null; // 테스트: null이면 신규 회원

        boolean is_empty = (store_vo == null);
        boolean is_open = false; 

        if (!is_empty) {
            is_open = (boolean) store_vo.get("is_open");
            model.addAttribute("store_vo", store_vo);
        } else {
            // 신규 회원일 때 500 에러 방지용 빈 객체
            model.addAttribute("store_vo", new HashMap<String, Object>());
        }

        model.addAttribute("is_empty", is_empty);
        model.addAttribute("is_open", is_open);
        
        return "owner/owner-dashboard";
    }

    /**
     * [GET] 가게 정보 설정 (500 에러 해결 포인트)
     */
    @GetMapping("/store/setting")
    public String storeSetting(Model model) {
        Map<String, Object> store_vo = new HashMap<>();
        
        // 타임리프 *{변수명}과 매핑될 초기값 필수 세팅
        store_vo.put("store_idx", 0);
        store_vo.put("store_name", "");
        store_vo.put("min_order_price", 0);
        store_vo.put("open_time", "09:00");
        store_vo.put("close_time", "22:00");
        store_vo.put("off_days", "");
        store_vo.put("store_desc", "");

        model.addAttribute("store_vo", store_vo);
        return "owner/store-setting"; 
    }

    /**
     * [POST] 매장 정보 저장 처리
     */
    @PostMapping("/store/update_proc")
    public String updateStoreProc(@RequestParam Map<String, Object> params) {
        // DB 저장 로직 (MyBatis 호출 등)
        return "redirect:/owner/dashboard";
    }

    /**
     * [POST] 지도 노출 상태 토글 (ON/OFF)
     */
    @PostMapping("/status/toggle")
    public String toggleMapStatus(@RequestParam("store_idx") int store_idx) {
        // DB: update store_info set is_open = not is_open where store_idx = ...
        return "redirect:/owner/dashboard";
    }

    // --- 기타 페이지 매핑 (이미지 파일 목록 일치) ---

    @GetMapping("/menu/management")
    public String menuManagement(Model model) {
        model.addAttribute("menu_list", new ArrayList<>()); 
        return "owner/menu-management"; 
    }

    @GetMapping("/menu/register")
    public String menuRegister() {
        return "owner/owner-menu-register"; 
    }

    @GetMapping("/category/setting")
    public String categorySetting(Model model) {
        model.addAttribute("category_list", new ArrayList<>()); 
        return "owner/category-setting";
    }

    @GetMapping("/order/detail")
    public String orderDetail(@RequestParam("order_idx") int order_idx, Model model) {
        model.addAttribute("order_idx", order_idx);
        return "owner/owner-order-detail"; 
    }
    
    /**
     * [GET] 실시간 주문 목록
     */
    @GetMapping("/order/list")
    public String orderList(@RequestParam(name = "status", defaultValue = "PENDING") String status, Model model) {
        // DB에서 해당 상태의 주문 리스트를 가져오는 로직
        // List<Map<String, Object>> order_list = orderService.selectOrderList(status);
        
        model.addAttribute("order_list", new ArrayList<>()); // 빈 리스트 테스트
        return "owner/owner-order-list";
    }

    /**
     * [POST] 주문 수락 처리
     */
    @PostMapping("/order/accept")
    public String acceptOrder(@RequestParam("order_idx") int order_idx) {
        // order_status를 'COOKING'으로 변경하는 DB 처리
        return "redirect:/owner/order/list?status=COOKING";
    }

    /**
     * [POST] 조리 완료 처리
     */
    @PostMapping("/order/complete_cook")
    public String completeCook(@RequestParam("order_idx") int order_idx) {
        // order_status를 'DELIVERING' 또는 'COMPLETED'로 변경
        return "redirect:/owner/order/list?status=COMPLETED";
    }
}