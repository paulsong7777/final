package com.moeats.controller.owner;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@Controller
@RequestMapping("/owner")
public class ViewOwnerController {

    /* [COMMON] 모든 페이지 사이드바 데이터 */
    @ModelAttribute
    public void commonSidebarData(Model model) {
        Map<String, Object> storeVo = new HashMap<>();
        storeVo.put("storeName", "모이츠 대구본점");
        storeVo.put("storeAddr", "대구광역시 중구 중앙대로 403");

        List<Map<String, Object>> menuList = new ArrayList<>();
        Map<String, Object> m = new HashMap<>();
        m.put("menuName", "스테이크 비빔밥");
        menuList.add(m);

        model.addAttribute("storeVo", storeVo);
        model.addAttribute("menuList", menuList);
    }

    /* 🖥️ [DASHBOARD] 대시보드 - 데이터가 안 나올 수 없게 모든 변수명 주입 */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("menu", "dash");

        // 실시간 주문 가상 데이터 생성
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> o = new HashMap<>();
        o.put("roomIdx", 20260406001L);
        o.put("roomStatus", "WAITING");
        o.put("orderMenu", "스테이크 비빔밥 외 1건");
        o.put("totalPrice", 24000);
        o.put("orderTime", "18:50");
        list.add(o);

        // HTML에서 어떤 이름을 쓰든 걸리도록 다 넣어줍니다.
        model.addAttribute("activeOrders", list);
        model.addAttribute("orders", list);
        model.addAttribute("orderList", list);
        
        return "views/owner/dashboard"; 
    }

    /* 📋 [ORDER DETAIL] 주문 상세 - 버튼 및 정보 복구 */
    @GetMapping("/order/detail")
    public String orderDetail(@RequestParam(value="roomIdx", required=false) Long roomIdx, Model model) {
        model.addAttribute("menu", "order");
        
        // 어떤 번호로 들어오든 테스트 가능하도록 즉석 생성
        Long id = (roomIdx != null) ? roomIdx : 20260406001L;

        Map<String, Object> order = new HashMap<>();
        order.put("roomIdx", id);
        order.put("roomStatus", "WAITING"); // [접수/거절] 버튼 활성화 핵심 조건
        order.put("customerName", "김모이츠");
        order.put("customerPhone", "010-1234-5678");
        order.put("deliveryAddr", "대구 중구 중앙대로 403 잇다빌딩");
        
        // 상세 메뉴 리스트
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("menuName", "스테이크 비빔밥");
        item.put("menuPrice", 12000);
        items.add(item);
        order.put("orderItems", items);

        model.addAttribute("order", order);
        return "views/owner/order-detail"; 
    }

    /* 🍴 [MAPPING] 나머지 메뉴 (누락 없이 가독성 정돈) */
    @GetMapping("/order/list") public String orderList(Model model) { 
        model.addAttribute("menu", "order");
        // 리스트 페이지에서도 데이터가 보이게 리스트 주입
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> o = new HashMap<>();
        o.put("roomIdx", 20260406001L); o.put("roomStatus", "WAITING"); o.put("orderMenu", "스테이크 비빔밥");
        list.add(o);
        model.addAttribute("orders", list);
        return "views/owner/order-list"; 
    }
    
    @GetMapping("/menu/management") public String menuManagement(Model model) { model.addAttribute("menu", "menu"); return "views/owner/menu-manage"; }
    @GetMapping("/menu/register") public String menuRegister(Model model) { model.addAttribute("menu", "menu"); return "views/owner/menu-register"; }
    @GetMapping("/category/setting") public String categorySetting(Model model) { model.addAttribute("menu", "category"); return "views/owner/category-setting"; }
    @GetMapping("/store/setting") public String storeSetting(Model model) { model.addAttribute("menu", "store"); return "views/owner/store-edit"; }
    @GetMapping("/review/management") public String reviewManagement(Model model) { model.addAttribute("menu", "review"); return "views/owner/review-management"; }
    @GetMapping("/sales/report") public String salesReport(Model model) { model.addAttribute("menu", "report"); return "views/owner/sales-report"; }

    @PostMapping("/category/register_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> categoryRegisterProc(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }
}