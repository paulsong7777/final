package com.moeats.controller.owner;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.csrf.CsrfToken;
import java.util.*;

import com.moeats.services.MenuService;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
@RequestMapping({"/owner", "/owners"}) // 접두사 유지
public class ViewOwnerController {

    @Autowired
    private MenuService menuService; // ✅ 서비스 주입

    // ==========================================
    // [COMMON] 모든 페이지 공통 데이터 (11개 메뉴 연동 및 Anti-Null)
    // ==========================================
    @ModelAttribute
    public void commonSidebarData(Model model) {
        // [Anti-Null] 기본 데이터 강제 할당
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
    // [MAIN] 대시보드 (비활성화 유지 - 삭제 엄금)
    // ==========================================
    /*
    // ✅ [비활성화 사유]: 대시보드 전용 컨트롤러(/members/dashboard)가 별도로 존재함.
    @GetMapping({"/dashboard", ""})
    public String dashboard(Model model) {
        model.addAttribute("menu", "dash");
        Map<String, Object> sidebarData = (Map<String, Object>) model.asMap().get("storeVo");
        if (sidebarData == null || sidebarData.isEmpty()) {
            return "redirect:/owner/store/new";
        }
        return "views/owner/dashboard"; 
    }
    */

    // ==========================================
    // [ORDER] 주문 관리
    // ==========================================
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
        return "views/owner/order-detail"; 
    }

    @GetMapping("/order/list")
    public String orderList(Model model) {
        model.addAttribute("menu", "order-list");
        return "views/owner/order-list";
    }

    @GetMapping("/order/status_move")
    public String statusMove(@RequestParam("roomIdx") Long roomIdx, @RequestParam("nextStatus") String nextStatus) {
        // ✅ [교정]: 배달 완료(COMPLETED) 시 /owners/dashboard-map으로 리다이렉트 (404 방지)
        if ("COMPLETED".equals(nextStatus)) return "redirect:/owners/dashboard-map";
        return "redirect:/owner/order/detail?roomIdx=" + roomIdx + "&status=" + nextStatus;
    }

    // ==========================================
    // [STORE] 매장 관리 (405 에러 해결 - GET 복구)
    // ==========================================
    @GetMapping("/store/new")
    public String storeCreatePage(HttpServletRequest request, Model model) {
        model.addAttribute("menu", "store-new");
        CsrfToken token = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (token != null) model.addAttribute("_csrf", token);
        model.addAttribute("storeVo", new HashMap<String, Object>()); 
        return "views/owner/store-create";
    }

    @PostMapping("/store/new")
    public String storeInsertProc() {
        return "redirect:/members/dashboard";
    }

    @GetMapping({"/store/setting", "/store/edit"})
    public String storeSetting(Model model) {
        model.addAttribute("menu", "store");
        return "views/owner/store-edit";
    }

    @PostMapping({"/store/setting", "/store/edit", "/store/update"})
    public String storeUpdateProc(@RequestParam Map<String, Object> params) {
        return "redirect:/members/dashboard";
    }

    // ==========================================
    // [MENU] 메뉴 관리
    // ==========================================
    @GetMapping({"/menu/register", "/menu/write"})
    public String menuRegisterPage(Model model) {
        model.addAttribute("menu", "menu-reg");
        return "views/owner/menu-register";
    }

    @PostMapping({"/menu/register", "/menu/write"})
    public String handleMenuRegister(
            @RequestParam("menuCategoryIdx") int menuCategoryIdx, 
            @RequestParam("menuName") String menuName,
            @RequestParam("menuPrice") int menuPrice, 
            @RequestParam(value="menuDescription", required=false) String menuDescription,
            @RequestParam(value="menuFile", required=false) MultipartFile menuFile) {
        return "redirect:/owner/menu/management";
    }

    @GetMapping("/menu/edit")
    public String menuEditPage(@RequestParam(value="menuIdx", required=false, defaultValue="0") int menuIdx, Model model) {
        model.addAttribute("menu", "menu-edit");
        
        if(menuIdx != 0) {
            model.addAttribute("menuVo", menuService.findByIdx(menuIdx));
        } else {
            model.addAttribute("menuVo", new HashMap<String, Object>());
        }
        
        return "views/owner/menu-edit"; 
    }

    @PostMapping("/menu/update")
    public String handleMenuUpdate(@RequestParam Map<String, Object> params,
                                 @RequestParam(value="menuFile", required=false) MultipartFile menuFile) {
        return "redirect:/owner/menu/management";
    }

    @GetMapping({"/menu/management", "/menu/manage"})
    public String menuManagement(Model model) {
        model.addAttribute("menu", "menu-mgmt");
        return "views/owner/menu-manage"; 
    }

    // ==========================================
    // [CATEGORY] 카테고리 관리 (비활성화 유지 - 삭제 엄금)
    // ==========================================
    /*
    // ✅ [비활성화 사유]: StoreMenuCategoryController 가 별도로 존재함.
    @GetMapping({"/category/setting", "/category/manage", "/category/list"})
    public String categorySetting(Model model) {
        model.addAttribute("menu", "category");
        return "views/owner/category-list"; 
    }
    @GetMapping("/category/register")
    public String categoryWritePage(Model model) {
        model.addAttribute("menu", "category-reg");
        return "views/owner/category-register"; 
    }
    */

    // ==========================================
    // [REVIEW & REPORT]
    // ==========================================
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

    @PostMapping("/order/status_update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> statusUpdate(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("success", true, "message", "상태 업데이트 완료"));
    }
    
    @GetMapping("/new-owner/guide")
    public String newOwnerGuide(Model model) {
        model.addAttribute("menu", "owner-guide");
        return "redirect:/owners/store/new";
    }
}