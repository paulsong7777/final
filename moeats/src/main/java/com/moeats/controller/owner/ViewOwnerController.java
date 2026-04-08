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
    // [COMMON] 모든 페이지 공통 데이터 (9개 메뉴 연동)
    // ==========================================
    @ModelAttribute
    public void commonSidebarData(Model model) {
        // 실제 운영 시에는 여기서 DB 조회를 통해 매장 유무를 확인합니다.
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
    // [MAIN] 대시보드 및 신규 회원 대응
    // ==========================================
    @GetMapping({"/dashboard", "/members/dashboard", ""})
    public String dashboard(Model model) {
        model.addAttribute("menu", "dash");
        
        /**
         * ✅ [사장님 요청 반영]: 대시보드 HTML 수정 없이 신규 매장 등록 폼 띄우기
         * 로직: 매장 정보(storeVo)가 null이면 대시보드 대신 등록 페이지로 강제 이동(Redirect)
         * [교정]: 500 에러 방지를 위해 model.asMap()을 통해 안전하게 추출합니다.
         */
        Map<String, Object> sidebarData = (Map<String, Object>) model.asMap().get("storeVo");
        
        // 만약 storeVo가 null이거나 정보가 비어있다면 등록 폼으로 보냅니다.
        if (sidebarData == null || sidebarData.isEmpty()) {
            return "redirect:/owners/store/new";
        }
        
        return "views/owner/dashboard"; 
    }

    // ==========================================
    // [ORDER] 주문 관리 (상태 변경 스위치 포함)
    // ==========================================
    
    // 2. 주문 상세 조회 (스위치 로직 핵심)
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

    // 3. 현재 주문 내역 (구 실시간 주문 확인)
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
    // [STORE] 매장 관리
    // ==========================================

    /**
     * 4. 신규 매장 등록 (중복 매핑 해결 버전)
     * 기존 StoreController의 /owners/store/new 와 충돌을 피하기 위해 
     * 주소를 /store/register 로 명확히 분리했으나, 사장님 환경에 맞춰 /store/new로 최종 유지
     */
    @GetMapping("/store/new")
    public String storeCreatePage(Model model) {
        model.addAttribute("menu", "store-new");
        model.addAttribute("storeVo", null); 
        return "views/owner/store-create";
    }

    // ✅ 해결: 405 에러 방지 - 등록 처리를 위한 POST 매핑 추가
    @PostMapping("/store/new")
    public String storeInsertProc() {
        // 등록 로직 수행 후 대시보드로 리다이렉트
        return "redirect:/owner/dashboard";
    }

    // 5. 매장 정보 수정
    @GetMapping({"/store/setting", "/store/edit"})
    public String storeSetting(Model model) {
        model.addAttribute("menu", "store");
        return "views/owner/store-edit";
    }

    // ==========================================
    // [MENU] 메뉴 관리 (404 방지 및 사장님 요청 순서 반영)
    // ==========================================
    
    // 6. 신규 메뉴 등록
    @GetMapping({"/menu/register", "/menu/write", "/owner-menu-register"})
    public String menuRegisterPage(Model model) {
        model.addAttribute("menu", "menu-reg");
        return "views/owner/menu-register";
    }

    // 7. 메뉴 수정 (신규 추가 및 404 에러 해결)
    @GetMapping("/menu/edit")
    public String menuEditPage(Model model) {
        model.addAttribute("menu", "menu-edit");
        return "views/owner/menu-edit";
    }

    // [참고] 기존 menu-manage는 메뉴 수정 기능으로 통합되어 삭제 또는 유지 가능
    @GetMapping({"/menu/management", "/menu/manage"})
    public String menuManagement(Model model) {
        model.addAttribute("menu", "menu-mgmt");
        return "views/owner/menu-manage"; 
    }

    @PostMapping({"/menu/register", "/menu/write"})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> handleMenuRegister(
            @RequestParam("menuCategoryIdx") int menuCategoryIdx, @RequestParam("menuName") String menuName,
            @RequestParam("menuPrice") int menuPrice, @RequestParam(value="menuDescription", required=false) String menuDescription,
            @RequestParam(value="menuFile", required=false) MultipartFile menuFile) {
        return ResponseEntity.ok(Map.of("success", true));
    }

    // 8. 카테고리 설정 (목록)
    @GetMapping({"/category/setting", "/category/manage", "/category/list"})
    public String categorySetting(Model model) {
        model.addAttribute("menu", "category");
        return "category/list"; 
    }

    // ✅ [추가] 신규 카테고리 등록 페이지 이동 (메뉴 연동용)
    @GetMapping("/category/register")
    public String categoryWritePage(Model model) {
        model.addAttribute("menu", "category-reg");
        return "category/write"; 
    }

    // ✅ [교정] Ambiguous handler 충돌 방지를 위해 기존 중복 메서드는 주석 유지
    /*
    @GetMapping("/store-menu-category/write")
    public String categoryWritePage(Model model) {
        model.addAttribute("menu", "category");
        return "category/write"; 
    }
    */

    // ==========================================
    // [REVIEW & REPORT]
    // ==========================================

    // 9. 리뷰 답변 관리
    @GetMapping("/review/management")
    public String reviewManagement(Model model) {
        model.addAttribute("menu", "review");
        return "views/owner/review-management";
    }

    // 10. 영업 리포트
    @GetMapping("/sales/report")
    public String salesReport(Model model) {
        model.addAttribute("menu", "report");
        return "views/owner/sales-report";
    }

    @PostMapping("/order/status_update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> statusUpdate(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(Map.of("success", true));
    }
    
    // ✅ [추가] 사이드바 하단 신규 점주 안내용 메뉴 매핑
    @GetMapping("/new-owner/guide")
    public String newOwnerGuide(Model model) {
        model.addAttribute("menu", "owner-guide");
        return "redirect:/owners/store/new"; // 가이드를 누르면 바로 등록으로 연결
    }
}