package com.moeats.controller.owner;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Controller
@RequestMapping({"/owner", "/owners"}) // 단수 복수 모두 대응
public class ViewOwnerController {

    // ==========================================
    // [COMMON] 모든 페이지 공통 데이터 (11개 메뉴 연동)
    // ==========================================
    @ModelAttribute
    public void commonSidebarData(Model model) {
        Map<String, Object> storeVo = new HashMap<>();
        storeVo.put("storeName", "모이츠 대구본점");
        storeVo.put("storeAddr", "대구광역시 중구 중앙대로 403");
        model.addAttribute("storeVo", storeVo);
        
        List<Map<String, Object>> categoryList = new ArrayList<>();
        categoryList.add(Map.of("menuCategoryIdx", 1, "menuCategoryName", "치킨"));
        categoryList.add(Map.of("menuCategoryIdx", 2, "menuCategoryName", "피자"));
        model.addAttribute("categoryList", categoryList);
    }

    // ==========================================
    // [MAIN] 대시보드
    // ==========================================
    @GetMapping({"/dashboard", "/members/dashboard", ""})
    public String dashboard(Model model) {
        model.addAttribute("menu", "dash");
        return "views/owner/dashboard"; 
    }

    // ==========================================
    // [ORDER] 주문 관리 (상태 변경 스위치 포함)
    // ==========================================
    
    // 주문 상세 (스위치 로직 핵심)
    @GetMapping("/order/detail")
    public String orderDetail(
            @RequestParam(value="roomIdx", required=false) Long roomIdx, 
            @RequestParam(value="status", required=false) String status, Model model) {
        
        model.addAttribute("menu", "order-detail");
        Long id = (roomIdx != null) ? roomIdx : 20260407001L;
        String currentStatus = (status != null) ? status : "ACCEPTED";

        Map<String, Object> order = new HashMap<>();
        order.put("roomIdx", id);
        order.put("roomStatus", currentStatus);
        order.put("storeName", "모이츠 대구본점");

        // [SWITCH] 현재 상태에 따른 버튼/라벨 텍스트 로직
        String nextStatus, btnText, statusLabel;
        switch (currentStatus) {
            case "ACCEPTED":   statusLabel = "접수완료"; nextStatus = "COOKING";    btnText = "조리 시작하기"; break;
            case "COOKING":    statusLabel = "조리중";   nextStatus = "DELIVERING"; btnText = "배달 시작하기"; break;
            case "DELIVERING": statusLabel = "배달중";   nextStatus = "COMPLETED";  btnText = "배달 완료처리"; break;
            case "COMPLETED":  statusLabel = "배달완료"; nextStatus = "DONE";       btnText = "종료된 주문"; break;
            default:           statusLabel = "대기중";   nextStatus = "ACCEPTED";   btnText = "주문 접수하기"; break;
        }

        order.put("statusLabel", statusLabel);
        order.put("nextStatus", nextStatus);
        order.put("btnText", btnText);

        model.addAttribute("order", order);
        model.addAttribute("orderRoom", order); 
        model.addAttribute("safeOrder", order);
        return "views/owner/order-detail"; 
    }

    @GetMapping("/order/list")
    public String orderList(Model model) {
        model.addAttribute("menu", "order-list");
        return "views/owner/order-list";
    }

    @GetMapping("/order/status_move")
    public String statusMove(@RequestParam("roomIdx") Long roomIdx, @RequestParam("nextStatus") String nextStatus) {
        if ("COMPLETED".equals(nextStatus)) return "redirect:/owner/dashboard";
        return "redirect:/owner/order/detail?roomIdx=" + roomIdx + "&status=" + nextStatus;
    }

    // ==========================================
    // [MENU] 메뉴 및 이미지 관리 (404 방지 다중 매핑)
    // ==========================================
    
    @GetMapping({"/menu/management", "/menu/manage", "/menu"})
    public String menuManagement(Model model) {
        model.addAttribute("menu", "menu-mgmt");
        return "views/owner/menu-manage"; 
    }

    @GetMapping({"/menu/register", "/menu/write", "/owner-menu-register"})
    public String menuRegisterPage(Model model) {
        model.addAttribute("menu", "menu-reg");
        return "views/owner/menu-register";
    }

    @GetMapping({"/menu/image/manage", "/menu-image-manage", "/menu-image-edit", "/menu/image/edit"})
    public String menuImageManage(Model model) {
        model.addAttribute("menu", "menu-img");
        return "views/owner/menu-image-manage";
    }

    @PostMapping({"/menu/register", "/menu/write"})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleMenuRegister(
            @RequestParam("menuCategoryIdx") int menuCategoryIdx, @RequestParam("menuName") String menuName,
            @RequestParam("menuPrice") int menuPrice, @RequestParam(value="menuDescription", required=false) String menuDescription,
            @RequestParam(value="menuFile", required=false) MultipartFile menuFile) {
        return ResponseEntity.ok(Map.of("success", true));
    }
/*
    // ==========================================
    // [CATEGORY] 설정 및 등록 (중복 에러 해결)
    // ==========================================
    
    // 1. 리스트 조회
    @GetMapping({"/category/setting", "/category/manage", "/category/list"})
    public String categoryList(Model model) {
        model.addAttribute("menu", "category");
        return "category/list"; 
    }

    // 2. 등록 페이지 이동 (충돌 방지를 위해 중복되는 /owners/store-menu-category/write 제거)
    @GetMapping({"/category/write", "/category/register"}) 
    public String categoryWritePage(Model model) {
        model.addAttribute("menu", "category");
        return "category/write"; 
    }

    // 3. [교정] 실제 등록 처리 (500 에러 알러트 해결 핵심)
    // HTML fetch(/owners/store-menu-category/write) 요청을 명확히 수신하기 위해 POST 전용 매핑 유지 및 @RequestParam 지정
    @PostMapping("/store-menu-category/write")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> categoryWriteProc(
            @RequestParam("categoryName") String categoryName) {
        System.out.println(">>> 카테고리 등록 요청 성공: " + categoryName);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/category/register_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> categoryQuickProc(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("success", true, "newIdx", (long)(Math.random() * 1000) + 100));
    }
*/
    // ==========================================
    // [STORE & REVIEW & REPORT]
    // ==========================================

    @GetMapping({"/store/setting", "/store/edit"})
    public String storeSetting(Model model) {
        model.addAttribute("menu", "store");
        return "views/owner/store-edit";
    }

    @GetMapping("/review/management")
    public String reviewManagement(Model model) {
        model.addAttribute("menu", "review");
        return "views/owner/review-management";
    }

    @GetMapping("/sales/report")
    public String salesReport(Model model) {
        model.addAttribute("menu", "report");
        return "views/owner/sales-report";
    }

    @GetMapping("/all-image-list")
    public String allImageList(Model model) {
        model.addAttribute("menu", "all-img");
        return "views/owner/all-image-list";
    }

    @PostMapping("/order/status_update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> statusUpdate(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("success", true));
    }
}