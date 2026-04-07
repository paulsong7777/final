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

    /* 🖥️ [DASHBOARD] */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("menu", "dash");
        List<Map<String, Object>> list = getMockOrders();
        model.addAttribute("activeOrders", list);
        model.addAttribute("orders", list);
        model.addAttribute("orderList", list);
        return "views/owner/dashboard"; 
    }

    /* 📋 [ORDER DETAIL] - 500 에러(storeName) 해결 핵심 */
    @GetMapping("/order/detail")
    public String orderDetail(@RequestParam(value="roomIdx", required=false) Long roomIdx, Model model) {
        model.addAttribute("menu", "order");
        Long id = (roomIdx != null) ? roomIdx : 20260406001L;

        Map<String, Object> order = new HashMap<>();
        order.put("roomIdx", id);
        order.put("roomStatus", "WAITING");
        order.put("storeName", "모이츠 대구본점"); // 500 에러 방지용 필드 추가
        order.put("customerName", "김모이츠");
        order.put("customerPhone", "010-1234-5678");
        order.put("deliveryAddr", "대구 중구 중앙대로 403 잇다빌딩");
        
        List<Map<String, Object>> items = new ArrayList<>();
        items.add(Map.of("menuName", "스테이크 비빔밥", "menuPrice", 12000));
        order.put("orderItems", items);

        model.addAttribute("order", order);
        model.addAttribute("safeOrder", order); // HTML에서 safeOrder를 써도 안 터지게 주입
        return "views/owner/order-detail"; 
    }

    /* 🍴 [MAPPINGS] 누락된 404 경로들 모두 복구 */
    @GetMapping("/order/list")
    public String orderList(Model model) { 
        model.addAttribute("menu", "order");
        model.addAttribute("orders", getMockOrders());
        return "views/owner/order-list"; 
    }
    
    @GetMapping("/menu/management") 
    public String menuManagement(Model model) { model.addAttribute("menu", "menu"); return "views/owner/menu-manage"; }
    
    @GetMapping("/menu/register") 
    public String menuRegister(Model model) { model.addAttribute("menu", "menu"); return "views/owner/menu-register"; }
    
    @GetMapping("/category/setting") 
    public String categorySetting(Model model) { model.addAttribute("menu", "category"); return "views/owner/category-setting"; }
    
    @GetMapping("/store/setting") 
    public String storeSetting(Model model) { model.addAttribute("menu", "store"); return "views/owner/store-edit"; }
    
    @GetMapping("/review/management") 
    public String reviewManagement(Model model) { model.addAttribute("menu", "review"); return "views/owner/review-management"; }
    
    @GetMapping("/sales/report") 
    public String salesReport(Model model) { model.addAttribute("menu", "report"); return "views/owner/sales-report"; }

    // 아까 404 났던 이미지 보관함 경로 복구
    @GetMapping("/all-image-list")
    public String allImageList(Model model) {
        model.addAttribute("menu", "img-list");
        return "views/owner/all-image-list";
    }

    @PostMapping("/category/register_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> categoryRegisterProc(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    /* 가상 데이터 생성 헬퍼 */
    private List<Map<String, Object>> getMockOrders() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> o = new HashMap<>();
        o.put("roomIdx", 20260406001L); 
        o.put("roomStatus", "WAITING"); 
        o.put("orderMenu", "스테이크 비빔밥");
        o.put("storeName", "모이츠 대구본점");
        list.add(o);
        return list;
    }
}