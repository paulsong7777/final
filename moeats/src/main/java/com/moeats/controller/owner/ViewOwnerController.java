package com.moeats.controller.owner;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/owner")
public class ViewOwnerController {

    /* ==========================================================
     * [SECTION 0] 설정 및 가상 데이터 저장소
     * ========================================================== */
    
    // [확인] 데이터 유무 테스트 스위치 (false: 데이터 있음 / true: 빈 데이터)
    private static final boolean IS_EMPTY_DATA_TEST = false; 

    private static final List<Map<String, Object>> orderList = new ArrayList<>();
    private static final List<Map<String, Object>> menuList = new ArrayList<>();
    private static final List<Map<String, Object>> categoryList = new ArrayList<>();
    private static final List<Map<String, Object>> reviewList = new ArrayList<>();
    private static final Map<String, Object> storeData = new HashMap<>();

    static { initMockData(); }

    /**
     * 가상 데이터 초기 세팅 (반복 테스트를 위해 상시 호출 가능)
     */
    private static void initMockData() {
        // [1] Store Data
        storeData.clear();
        storeData.put("storeIdx", 1);
        storeData.put("storeName", "모이츠 대구본점");
        storeData.put("storeDescription", "모이면 더 맛있는 Mo-eats");
        storeData.put("storePhone", "010-1234-5678");
        storeData.put("minimumOrderAmount", 15000);

        // [2] Menu Data
        menuList.clear();
        menuList.add(createMenu(101, "황금올리브 치킨", 20000, "치킨", "AVAILABLE", "바삭함의 대명사", 1, 1));

        // [3] Order Data (무한 반복 테스트를 위해 ACCEPTED 상태로 초기화)
        orderList.clear();
        orderList.add(createOrder(2001, "ORD-2026-001", "17:30", "황금올리브 치킨", 20000, "ACCEPTED", "대구 중구 중앙대로 101"));
        orderList.add(createOrder(2002, "ORD-2026-002", "17:45", "양념치킨 세트", 22000, "ACCEPTED", "대구 수성구 범어동 202"));
        orderList.add(createOrder(2003, "ORD-2026-003", "18:10", "치즈볼 & 콜라", 8500, "ACCEPTED", "대구 남구 봉덕동 303"));

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
        // [반복 테스트 기능] 대시보드 진입 시마다 데이터를 초기화하여 무한 테스트 환경 제공
        initMockData(); 

        model.addAttribute("orderList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : orderList);
        model.addAttribute("storeVo", IS_EMPTY_DATA_TEST ? new HashMap<>() : storeData);
        model.addAttribute("menu", "dash");
        return "owner/owner-dashboard";
    }

    @GetMapping("/order/detail")
    public String orderDetail(@RequestParam(value="roomIdx", required = false) String roomIdx, Model model) {
        // 상세 페이지에서 바로 새로고침하여 테스트할 경우를 대비해 데이터가 없으면 초기화
        if (orderList.isEmpty()) initMockData();

        Map<String, Object> target = orderList.stream()
                .filter(o -> String.valueOf(o.get("roomIdx")).equals(roomIdx))
                .findFirst()
                .orElse(!orderList.isEmpty() ? orderList.get(0) : new HashMap<>());
        
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
    public String menuEdit(@RequestParam(value="menuIdx", defaultValue="101") String menuIdx, Model model) {
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

    @GetMapping("/sales/report") 
    public String salesReport(Model model) { 
        model.addAttribute("menu", "report"); 
        return "owner/sales-report"; 
    }

    @GetMapping("/support/notice") 
    public String supportNotice(Model model) { 
        model.addAttribute("menu", "notice"); 
        return "owner/owner-dashboard"; 
    }

    /* ==========================================================
     * [SECTION 2] 비동기 처리 API (POST Mapping)
     * ========================================================== */

    @PostMapping("/status_update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateStatus(@RequestBody Map<String, Object> params) {
        String targetIdx = String.valueOf(params.get("roomIdx"));
        String nextStatus = String.valueOf(params.get("roomStatus"));
        
        boolean isUpdated = false;
        for (Map<String, Object> order : orderList) {
            if (String.valueOf(order.get("roomIdx")).equals(targetIdx)) {
                order.put("roomStatus", nextStatus);
                isUpdated = true;
                break;
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", isUpdated);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/menu/register_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> menuRegisterProc(@RequestParam("categoryIdx") int catIdx, @RequestParam("menuName") String name, @RequestParam("menuPrice") int price, @RequestParam(value="menuDescription", required=false) String desc, @RequestParam(value="menuFile", required=false) MultipartFile file) {
        menuList.add(createMenu(menuList.size() + 101, name, price, "카테고리", "AVAILABLE", desc, catIdx, menuList.size() + 1));
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
        return ResponseEntity.ok(Collections.singletonMap("success", updated));
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

/**
 * 테스트용 독립 컨트롤러 (클래스 유지)
 */
@Controller
class TestMenuController {
    @GetMapping("/menu/register_backup")
    public String viewBackupPage(Model model) {
        List<Map<String, Object>> mockCats = new ArrayList<>();
        Map<String, Object> cat1 = new HashMap<>();
        cat1.put("categoryIdx", 1);
        cat1.put("categoryName", "치킨(테스트)");
        mockCats.add(cat1);

        model.addAttribute("categoryList", mockCats);
        model.addAttribute("menu", "menu_none");
        return "owner/menu-register"; 
    }
}