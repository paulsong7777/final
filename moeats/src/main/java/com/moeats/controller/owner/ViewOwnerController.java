package com.moeats.controller.owner;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/owner")
public class ViewOwnerController {

	// 데이터 유무 테스트 스위치 - 기존회원 로그인시 가상 데이터 false, 신규회원(실제DB연동) true 
	// db 로 연동 시 true 로 바꿔서 사용해야 함.
    private static boolean IS_EMPTY_DATA_TEST = false; 
    // private static boolean IS_EMPTY_DATA_TEST = true; 

    private static List<Map<String, Object>> orderList = new ArrayList<>();
    private static List<Map<String, Object>> menuList = new ArrayList<>();
    private static List<Map<String, Object>> categoryList = new ArrayList<>();
    private static List<Map<String, Object>> reviewList = new ArrayList<>();
    private static Map<String, Object> storeData = new HashMap<>();

    static { initMockData(); }

    private static void initMockData() {
        // [1] Store Data
        storeData.clear();
        storeData.put("storeIdx", 1);
        storeData.put("storeName", "모이츠 대구본점");
        storeData.put("storeDescription", "모이면 더 맛있는 Mo-eats");
        storeData.put("storePhone", "010-1234-5678");
        storeData.put("minimumOrderAmount", 15000);
        storeData.put("latitude", 35.8714);
        storeData.put("longitude", 128.6014);

        // [2] Menu Data
        menuList.clear();
        menuList.add(createMenu(101, "황금올리브 치킨", 20000, "치킨", "AVAILABLE", "바삭함의 대명사", 1, 1));

        // [3] Order Data
        orderList.clear();
        orderList.add(createOrder(2001, "ORD-2026-001", "17:30", "황금올리브 치킨", 20000, "PENDING", "대구 중구 중앙대로 101"));
        orderList.add(createOrder(2002, "ORD-2026-002", "17:45", "양념치킨 세트", 22000, "ACCEPTED", "대구 수성구 범어동 202"));
        orderList.add(createOrder(2003, "ORD-2026-003", "18:10", "치즈볼 & 콜라", 8500, "COOKING", "대구 남구 봉덕동 303"));

        // [4] Category Data
        categoryList.clear();
        categoryList.add(createMap("categoryIdx", 1, "categoryName", "치킨", "menuCount", 1));

        // [5] Review Data
        reviewList.clear();
        reviewList.add(createReview(501, "치킨무많이", "2026-03-29", 5, "황금올리브 치킨", "배달도 빠르고 바삭해요!", ""));
        reviewList.add(createReview(502, "다이어터", "2026-03-28", 4, "치즈볼", "맛있는데 콜라가 덜 시원해요.", "정성스런 리뷰 감사합니다!"));
    }

    /* ==========================================================
     * [SECTION 1] 페이지 이동 (GET Mapping)
     * ========================================================== */

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        initMockData(); //  새로고침 시 데이터 초기화
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
        initMockData(); // 새로고침 시 데이터 초기화
        model.addAttribute("orderList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : orderList);
        model.addAttribute("menu", "history");
        return "owner/owner-order-list";
    }

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

    @GetMapping("/category/setting")
    public String categorySetting(Model model) {
        model.addAttribute("categoryList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : categoryList);
        model.addAttribute("menu", "category");
        return "owner/category-setting";
    }

    @GetMapping("/store/setting")
    public String storeSetting(Model model) {
        model.addAttribute("storeVo", IS_EMPTY_DATA_TEST ? new HashMap<>() : storeData);
        model.addAttribute("menu", "store");
        return "owner/store-setting";
    }

    @GetMapping("/review/review-management")
    public String reviewManagement(Model model) {
        model.addAttribute("reviewList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : reviewList);
        model.addAttribute("menu", "review");
        return "owner/review-management";
    }

    @GetMapping("/sales/report") public String salesReport(Model model) { model.addAttribute("menu", "report"); return "owner/sales-report"; }
    @GetMapping("/support/notice") public String supportNotice(Model model) { model.addAttribute("menu", "notice"); return "owner/owner-dashboard"; }
    @GetMapping("/settlement/info") public String settlementInfo(Model model) { model.addAttribute("menu", "settle"); return "owner/owner-dashboard"; }

    /* ==========================================================
     * [SECTION 2] 비동기 처리 API (POST Mapping)
     * ========================================================== */

    @PostMapping("/status_update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatus(@RequestBody Map<String, Object> params) {
        String targetIdx = String.valueOf(params.get("roomIdx"));
        String nextStatus = String.valueOf(params.get("roomStatus"));
        orderList.forEach(o -> { if(String.valueOf(o.get("roomIdx")).equals(targetIdx)) o.put("roomStatus", nextStatus); });
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @PostMapping("/store/update_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> storeUpdateProc(@RequestParam("storeName") String storeName, @RequestParam(value = "storeImg", required = false) MultipartFile storeImg) {
        storeData.put("storeName", storeName);
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @PostMapping("/category/insert_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> categoryInsertProc(@RequestBody Map<String, Object> params) {
        String name = String.valueOf(params.get("categoryName"));
        categoryList.add(createMap("categoryIdx", categoryList.size() + 1, "categoryName", name, "menuCount", 0));
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @PostMapping("/category/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> categoryDelete(@RequestBody Map<String, Object> params) {
        String targetIdx = String.valueOf(params.get("categoryIdx"));
        categoryList.removeIf(c -> String.valueOf(c.get("categoryIdx")).equals(targetIdx));
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @PostMapping("/menu/register_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> menuRegisterProc(@RequestParam("categoryIdx") int catIdx, @RequestParam("menuName") String name, @RequestParam("menuPrice") int price, @RequestParam(value="menuDescription", required=false) String desc, @RequestParam(value="menuFile", required=false) MultipartFile file) {
        menuList.add(createMenu(menuList.size() + 101, name, price, "카테고리", "HIDDEN", desc, catIdx, menuList.size() + 1));
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @PostMapping("/menu/toggle-soldout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSoldOut(@RequestBody Map<String, Object> params) {
        String targetIdx = String.valueOf(params.get("menuIdx"));
        menuList.forEach(m -> {
            if(String.valueOf(m.get("menuIdx")).equals(targetIdx)) {
                String cur = String.valueOf(m.get("menuStatus"));
                m.put("menuStatus", "AVAILABLE".equals(cur) ? "SOLD_OUT" : "AVAILABLE");
            }
        });
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @PostMapping("/review/reply_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> replyProc(@RequestBody Map<String, Object> params) {
        String idx = String.valueOf(params.get("reviewIdx"));
        String content = String.valueOf(params.get("replyContent"));
        boolean updated = false;

        for (Map<String, Object> r : reviewList) {
            if (String.valueOf(r.get("reviewIdx")).equals(idx)) {
                r.put("replyContent", content);
                r.put("replyRegDate", LocalDateTime.now().toString()); 
                updated = true;
                break;
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("success", updated);
        return ResponseEntity.ok(result);
    }

    /* ==========================================================
     * [SECTION 3] 가상 데이터 생성 헬퍼 함수
     * ========================================================== */

    private static Map<String, Object> createOrder(int idx, String code, String date, String menu, int price, String status, String addr) {
        Map<String, Object> o = new HashMap<>();
        o.put("roomIdx", idx); o.put("orderCode", code); o.put("orderDate", date);
        o.put("menuName", menu); o.put("totalPrice", price); o.put("roomStatus", status);
        o.put("deliveryAddress1", addr); o.put("menuImg", "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=200");
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

    private static Map<String, Object> createReview(int idx, String nick, String date, int rate, String menu, String content, String reply) {
        Map<String, Object> r = new HashMap<>();
        r.put("reviewIdx", idx); r.put("memberNickname", nick); r.put("regDate", date);
        r.put("reviewRating", rate); r.put("menuName", menu); r.put("reviewContent", content);
        r.put("replyContent", reply); r.put("replyRegDate", "2026-03-30");
        return r;
    }

    private static Map<String, Object> createMap(Object... args) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) m.put(args[i].toString(), args[i + 1]);
        return m;
    }
    
    
}