package com.moeats.controller.owner;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/owner")
public class ViewOwnerController {

    // 🚩 데이터 유무 테스트 스위치
    private static boolean IS_EMPTY_DATA_TEST = false; 

    private static List<Map<String, Object>> orderList = new ArrayList<>();
    private static List<Map<String, Object>> menuList = new ArrayList<>();
    private static List<Map<String, Object>> categoryList = new ArrayList<>();
    private static Map<String, Object> storeData = new HashMap<>();

    static { initMockData(); }

    private static void initMockData() {
        // [Store]
        storeData.clear();
        storeData.put("storeIdx", 1);
        storeData.put("storeName", "모이츠 대구본점");
        storeData.put("storeDescription", "사람과 맛을 잇다, EATDA");
        storeData.put("storePhone", "010-1234-5678");
        storeData.put("minimumOrderAmount", 15000);
        storeData.put("latitude", 35.8714);
        storeData.put("longitude", 128.6014);

        // [Menu]
        menuList.clear();
        menuList.add(createMenu(101, "황금올리브 치킨", 20000, "치킨", "AVAILABLE", "바삭함의 대명사", 1, 1));

        // ⭐ [해결] 누락되었던 가상 주문 데이터(orderList) 생성
        orderList.clear();
        orderList.add(createOrder(2001, "ORD-2026-001", "17:30", "황금올리브 치킨", 20000, "PENDING", "대구 중구 중앙대로 101"));
        orderList.add(createOrder(2002, "ORD-2026-002", "17:45", "양념치킨 세트", 22000, "ACCEPTED", "대구 수성구 범어동 202"));
        orderList.add(createOrder(2003, "ORD-2026-003", "18:10", "치즈볼 & 콜라", 8500, "COOKING", "대구 남구 봉덕동 303"));

        categoryList.clear();
        categoryList.add(createMap("categoryIdx", 1, "categoryName", "치킨"));
    }

    /* ==========================================================
     * [SECTION 1] 대시보드 및 주문 상세
     * ========================================================== */

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("orderList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : orderList);
        model.addAttribute("storeVo", IS_EMPTY_DATA_TEST ? new HashMap<>() : storeData);
        model.addAttribute("menu", "dash");
        return "owner/owner-dashboard";
    }

    @GetMapping("/order/detail")
    public String orderDetail(@RequestParam("roomIdx") String roomIdx, Model model) {
        Map<String, Object> target = orderList.stream()
                .filter(o -> String.valueOf(o.get("roomIdx")).equals(roomIdx))
                .findFirst().orElse(orderList.isEmpty() ? new HashMap<>() : orderList.get(0));
        model.addAttribute("order", IS_EMPTY_DATA_TEST ? new HashMap<>() : target);
        model.addAttribute("menu", "dash");
        return "owner/owner-order-detail";
    }

    @GetMapping("/order/history")
    public String orderHistory(Model model) {
        model.addAttribute("orderList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : orderList);
        model.addAttribute("menu", "history");
        return "owner/owner-order-list";
    }

    /* ==========================================================
     * [SECTION 2] 메뉴 관리 (CRUD)
     * ========================================================== */

    @GetMapping("/menu/management")
    public String menuManagement(Model model) {
        model.addAttribute("menuList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : menuList);
        model.addAttribute("menu", "menu_list");
        return "owner/menu-management";
    }

    @GetMapping("/menu/register")
    public String menuRegister(Model model) {
        model.addAttribute("categoryList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : categoryList);
        model.addAttribute("menu", "menu_reg");
        return "owner/owner-menu-register";
    }

    @GetMapping("/menu/edit")
    public String menuEdit(@RequestParam("menuIdx") String menuIdx, Model model) {
        Map<String, Object> target = menuList.stream()
                .filter(m -> String.valueOf(m.get("menuIdx")).equals(menuIdx))
                .findFirst().orElse(menuList.isEmpty() ? new HashMap<>() : menuList.get(0));
        model.addAttribute("menuVo", IS_EMPTY_DATA_TEST ? new HashMap<>() : target);
        model.addAttribute("categoryList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : categoryList);
        model.addAttribute("menu", "menu_list");
        return "owner/owner-menu-edit";
    }

    /* ==========================================================
     * [SECTION 3] 설정 및 비동기 API
     * ========================================================== */

    @PostMapping("/status_update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatus(@RequestBody Map<String, Object> params) {
        String targetIdx = String.valueOf(params.get("roomIdx"));
        String nextStatus = String.valueOf(params.get("roomStatus"));
        orderList.forEach(o -> { if(String.valueOf(o.get("roomIdx")).equals(targetIdx)) o.put("roomStatus", nextStatus); });
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @PostMapping("/menu/register_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> menuRegisterProc(
            @RequestParam("categoryIdx") int categoryIdx, @RequestParam("menuName") String menuName,
            @RequestParam("menuPrice") int menuPrice, @RequestParam(value = "menuDescription", required = false) String menuDescription,
            @RequestParam(value = "menuFile", required = false) MultipartFile menuFile) {
        
        int newIdx = menuList.size() + 101;
        Map<String, Object> newMenu = createMenu(newIdx, menuName, menuPrice, "카테고리", "HIDDEN", menuDescription, categoryIdx, newIdx);
        if (menuFile != null && !menuFile.isEmpty()) newMenu.put("menuImg", "/uploads/" + menuFile.getOriginalFilename());
        menuList.add(newMenu);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @PostMapping("/menu/toggle-soldout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSoldOut(@RequestBody Map<String, Object> params) {
        String targetIdx = String.valueOf(params.get("menuIdx"));
        for (Map<String, Object> menu : menuList) {
            if (String.valueOf(menu.get("menuIdx")).equals(targetIdx)) {
                String current = String.valueOf(menu.get("menuStatus"));
                menu.put("menuStatus", "AVAILABLE".equals(current) ? "SOLD_OUT" : "AVAILABLE");
                break;
            }
        }
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @GetMapping("/category/setting") public String categorySetting(Model model) { model.addAttribute("categoryList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : categoryList); model.addAttribute("menu", "category"); return "owner/category-setting"; }
    @GetMapping("/store/setting") public String storeSetting(Model model) { model.addAttribute("storeVo", IS_EMPTY_DATA_TEST ? new HashMap<>() : storeData); model.addAttribute("menu", "store"); return "owner/store-setting"; }
    @GetMapping("/review/review-management") public String reviewManagement(Model model) { model.addAttribute("menu", "review"); return "owner/review-management"; }
    @GetMapping("/sales/report") public String salesReport(Model model) { model.addAttribute("menu", "report"); return "owner/sales-report"; }
    @GetMapping("/support/notice") public String supportNotice(Model model) { model.addAttribute("menu", "notice"); return "owner/owner-dashboard"; }
    @GetMapping("/settlement/info") public String settlementInfo(Model model) { model.addAttribute("menu", "settle"); return "owner/owner-dashboard"; }

    /* ==========================================================
     * [UTIL] 헬퍼 메서드
     * ========================================================== */
    private static Map<String, Object> createOrder(int idx, String code, String date, String menu, int price, String status, String addr) {
        Map<String, Object> o = new HashMap<>();
        o.put("roomIdx", idx); o.put("orderCode", code); o.put("orderDate", date);
        o.put("menuName", menu); o.put("totalPrice", price); o.put("roomStatus", status);
        o.put("deliveryAddress1", addr);
        o.put("menuImg", "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=200");
        return o;
    }

    private static Map<String, Object> createMenu(int idx, String name, int price, String cat, String status, String desc, int catIdx, int displayOrder) {
        Map<String, Object> m = new HashMap<>();
        m.put("menuIdx", idx); m.put("menuName", name); m.put("menuPrice", price);
        m.put("categoryName", cat); m.put("menuStatus", status); m.put("menuDescription", desc);
        m.put("categoryIdx", catIdx); m.put("displayOrder", displayOrder);
        m.put("menuImg", "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=200");
        return m;
    }

    private static Map<String, Object> createMap(Object... args) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) m.put(args[i].toString(), args[i + 1]);
        return m;
    }
}