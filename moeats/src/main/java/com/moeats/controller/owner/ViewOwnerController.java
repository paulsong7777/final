package com.moeats.controller.owner;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

/**
 * [MoEats 점주 서비스 최종 통합 - 무결성 원칙 적용 완료]
 * 1. Layout: Fragments 공통화 대응
 * 2. AJAX: Fetch API 및 @ResponseBody 표준화
 * 3. Anti-Null: HashMap['key'] 접근 및 엘비스 연산자 대응
 * 4. Security: POST 고정 및 CSRF 토큰 수신
 * 5. Map: 위경도 및 매장 데이터 표준화
 * 6. Rule: roomIdx, roomStatus, menuIdx 명칭 통일
 */
@Controller
@RequestMapping("/owner")
public class ViewOwnerController {

    /* [철칙] 테스트 데이터 비우기 스위치 (true 시 빈 화면 테스트 가능) */
    private static boolean IS_EMPTY_DATA_TEST = false; 

    private static Map<String, Object> storeData = new HashMap<>(); 
    private static List<Map<String, Object>> orderList = new ArrayList<>();
    private static List<Map<String, Object>> categoryList = new ArrayList<>();
    private static List<Map<String, Object>> menuList = new ArrayList<>(); 
    private static List<Map<String, Object>> reviewList = new ArrayList<>();

    static { initMockData(); }

    private static void initMockData() {
        // [Map] 매장 정보 설정
        storeData.put("storeIdx", 101);
        storeData.put("storeName", "모이츠 대구본점");
        storeData.put("latitude", 35.8714);
        storeData.put("longitude", 128.6014);
        storeData.put("minimumOrderAmount", 15000);
        storeData.put("openTime", "09:00");
        storeData.put("closeTime", "22:00");
        storeData.put("offDays", "연중무휴");
        storeData.put("storeDescription", "최고의 맛을 보장하는 모이츠 대구본점입니다.");

        // [Rule] 실시간 주문 리스트
        orderList.add(createOrder(2001, "ORD-A1", "바삭 후라이드 치킨", "대구 중구 중앙대로 403", "PENDING", "21,000", "14:20", "2026-03-26 14:20"));
        orderList.add(createOrder(2002, "ORD-B2", "양념 반 후라이드 반", "대구 수성구 달구벌대로 21", "PREPARING", "15,500", "14:45", "2026-03-26 14:45"));
        
        // 카테고리 리스트
        categoryList.add(createMap("categoryIdx", 1, "categoryName", "치킨", "menuCount", 2));
        categoryList.add(createMap("categoryIdx", 2, "categoryName", "피자", "menuCount", 1));
        
        // 메뉴 리스트 (menuImg 키 강제 할당으로 500 에러 방지)
        menuList.add(createMap("menuIdx", 501, "categoryIdx", 1, "categoryName", "치킨", "menuName", "황금올리브", "menuPrice", 20000, "isSoldOut", "N", "menuImg", ""));
        menuList.add(createMap("menuIdx", 502, "categoryIdx", 1, "categoryName", "치킨", "menuName", "양념치킨", "menuPrice", 21000, "isSoldOut", "Y", "menuImg", ""));

        // 리뷰 리스트
        reviewList.add(createMap("reviewIdx", 301, "memberNickname", "치킨러버", "reviewRating", 5, "reviewContent", "맛있어요!", "regDate", "2026-03-25", "replyContent", "감사합니다!", "replyRegDate", "2026-03-26", "menuName", "황금올리브"));
    }

    /* ----------------------------------------------------------
     * [1] View Mapping (GET) - 페이지 전환
     * ---------------------------------------------------------- */
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("storeVo", IS_EMPTY_DATA_TEST ? new HashMap<>() : storeData);
        model.addAttribute("orderList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : orderList);
        model.addAttribute("menu", "dash");
        return "owner/owner-dashboard"; 
    }

    @GetMapping("/order/detail")
    public String orderDetail(@RequestParam(value="roomIdx", required=false) Integer roomIdx, Model model) {
        Map<String, Object> order = orderList.stream()
                .filter(o -> o.get("roomIdx").equals(roomIdx))
                .findFirst().orElse(orderList.get(0));
        model.addAttribute("orderVo", order);
        return "owner/owner-order-detail";
    }

    @GetMapping("/order/history")
    public String orderHistory(Model model) {
        model.addAttribute("orderList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : orderList);
        model.addAttribute("menu", "history");
        return "owner/owner-order-list";
    }

    @GetMapping("/menu/management")
    public String menuManagement(Model model) {
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("menuList", menuList);
        model.addAttribute("menu", "menu");
        return "owner/menu-management";
    }

    @GetMapping({"/menu/register", "/menu/edit"})
    public String menuForm(@RequestParam(value="menuIdx", required=false) Integer menuIdx, Model model) {
        if(menuIdx != null) {
            Map<String, Object> target = menuList.stream()
                    .filter(m -> m.get("menuIdx").equals(menuIdx))
                    .findFirst().orElse(menuList.get(0));
            model.addAttribute("menuVo", target); 
        }
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("menu", "menu");
        return menuIdx == null ? "owner/owner-menu-register" : "owner/owner-menu-edit";
    }

    @GetMapping("/category/setting")
    public String categorySetting(Model model) {
        model.addAttribute("categoryList", categoryList);
        model.addAttribute("menu", "category");
        return "owner/category-setting";
    }

    @GetMapping("/review/review-management")
    public String reviewManagement(Model model) {
        model.addAttribute("reviewList", reviewList);
        model.addAttribute("menu", "review");
        return "owner/review-management";
    }

    @GetMapping("/sales/report")
    public String salesReport(Model model) {
        model.addAttribute("totalSales", 550000);
        model.addAttribute("orderCount", 42);
        model.addAttribute("cancelCount", 2);
        model.addAttribute("avgRating", 4.8);
        model.addAttribute("popularMenuList", menuList);
        model.addAttribute("reportDate", "2026-03-26");
        model.addAttribute("chartLabels", Arrays.asList("09시", "12시", "15시", "18시", "21시"));
        model.addAttribute("chartData", Arrays.asList(5, 12, 8, 20, 15));
        model.addAttribute("menu", "report");
        return "owner/sales-report";
    }

    @GetMapping("/store/setting")
    public String storeSetting(Model model) {
        model.addAttribute("storeVo", storeData);
        model.addAttribute("menu", "store");
        return "owner/store-setting";
    }

    @GetMapping("/settlement/info")
    public String settlementInfo(Model model) {
        model.addAttribute("menu", "settle");
        return "owner/owner-dashboard"; // 파일 생성 전 임시 리다이렉트
    }

    /* ----------------------------------------------------------
     * [2] AJAX API (POST) - 비동기 데이터 처리
     * ---------------------------------------------------------- */

    @PostMapping("/category/insert_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> insertCategory(@RequestBody Map<String, Object> params) {
        categoryList.add(createMap("categoryIdx", categoryList.size() + 1, "categoryName", params.get("categoryName"), "menuCount", 0));
        return ResponseEntity.ok(createMap("success", true, "message", "카테고리 등록 성공"));
    }

    @PostMapping("/category/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCategory(@RequestBody Map<String, Object> params) {
        categoryList.removeIf(cat -> cat.get("categoryIdx").toString().equals(params.get("categoryIdx").toString()));
        return ResponseEntity.ok(createMap("success", true));
    }

    @PostMapping("/menu/toggle-soldout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSoldOut(@RequestBody Map<String, Object> params) {
        return ResponseEntity.ok(createMap("success", true));
    }

    @PostMapping({"/menu/register_proc", "/menu/edit_proc"})
    @ResponseBody
    public ResponseEntity<Map<String, Object>> menuProc(
            @RequestParam(value="menuFile", required=false) MultipartFile file, 
            @RequestParam Map<String, Object> params) {
        return ResponseEntity.ok(createMap("success", true));
    }

    @PostMapping("/menu/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteMenu(@RequestBody Map<String, Object> params) {
        return ResponseEntity.ok(createMap("success", true));
    }

    @PostMapping("/order/status-update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> statusUpdate(@RequestBody Map<String, Object> params) {
        return ResponseEntity.ok(createMap("success", true));
    }

    @PostMapping("/order/history-filter")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> historyFilter(@RequestBody Map<String, Object> params) {
        return ResponseEntity.ok(orderList); 
    }

    @PostMapping("/review/reply_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> replyProc(@RequestBody Map<String, Object> params) {
        return ResponseEntity.ok(createMap("success", true));
    }

    @PostMapping("/store/update_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> storeUpdate(@RequestBody Map<String, Object> params) {
        return ResponseEntity.ok(createMap("success", true));
    }

    /* [Util] 데이터 생성 도우미 */
    private static Map<String, Object> createOrder(int idx, String code, String menu, String addr, String status, String price, String time, String reg) {
        Map<String, Object> m = new HashMap<>();
        m.put("roomIdx", idx); m.put("roomCode", code); m.put("menuName", menu);
        m.put("deliveryAddress1", addr); m.put("roomStatus", status);
        m.put("totalPrice", price.replace(",", "")); m.put("orderTime", time); m.put("roomRegDate", reg);
        return m;
    }

    private static Map<String, Object> createMap(Object... args) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) m.put(args[i].toString(), args[i + 1]);
        return m;
    }
}