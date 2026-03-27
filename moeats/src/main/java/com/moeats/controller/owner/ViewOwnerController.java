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
        // [Order] 변수명: roomIdx, orderCode, orderDate, menuName, totalPrice, roomStatus, deliveryAddress1, menuImg
        orderList.clear();
        orderList.add(createOrder(2001, "ORD-001", "14:00", "황금올리브 치킨", 20000, "PENDING", "대구 중구 중앙대로 101"));

        // [Menu] 변수명: menuIdx, menuName, menuPrice, categoryName, menuStatus, menuDescription, categoryIdx, menuImg
        menuList.clear();
        menuList.add(createMenu(101, "황금올리브 치킨", 20000, "치킨", "ON_SALE", "바삭함의 대명사", 1));

        storeData.put("latitude", 35.8714);
        storeData.put("longitude", 128.6014);
        storeData.put("storeName", "모이츠 대구본점");

        categoryList.clear();
        categoryList.add(createMap("categoryIdx", 1, "categoryName", "치킨"));
    }

    /* ==========================================================
     * [SECTION 1] 메뉴 관리 (HTML name 속성과 변수명 100% 일치)
     * ========================================================== */

    @GetMapping("/menu/management")
    public String menuManagement(Model model) {
        model.addAttribute("menuList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : menuList);
        model.addAttribute("menu", "menu_list");
        return "owner/menu-management";
    }

    @PostMapping("/menu/register_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> menuRegisterProc(
            @RequestParam("categoryIdx") int categoryIdx,
            @RequestParam("menuName") String menuName,
            @RequestParam("menuPrice") int menuPrice,
            @RequestParam(value = "menuDescription", required = false) String menuDescription,
            @RequestParam(value = "menuFile", required = false) MultipartFile menuFile) {

        int newIdx = menuList.size() + 101;
        // HTML에서 넘어온 변수명 그대로 Map에 저장
        Map<String, Object> newMenu = createMenu(newIdx, menuName, menuPrice, "카테고리", "ON_SALE", menuDescription, categoryIdx);
        
        if (menuFile != null && !menuFile.isEmpty()) {
            newMenu.put("menuImg", "/uploads/" + menuFile.getOriginalFilename());
        }
        menuList.add(newMenu);
        
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @GetMapping("/menu/edit")
    public String menuEdit(@RequestParam("menuIdx") String menuIdx, Model model) {
        Map<String, Object> target = menuList.stream()
                .filter(m -> String.valueOf(m.get("menuIdx")).equals(menuIdx))
                .findFirst().orElse(menuList.get(0));
        
        model.addAttribute("menuVo", IS_EMPTY_DATA_TEST ? new HashMap<>() : target);
        model.addAttribute("categoryList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : categoryList);
        model.addAttribute("menu", "menu_list");
        return "owner/owner-menu-edit";
    }

    @PostMapping("/menu/edit_proc")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> menuEditProc(
            @RequestParam("menuIdx") int menuIdx,
            @RequestParam("categoryIdx") int categoryIdx,
            @RequestParam("menuName") String menuName,
            @RequestParam("menuPrice") int menuPrice,
            @RequestParam(value = "menuDescription", required = false) String menuDescription,
            @RequestParam(value = "menuFile", required = false) MultipartFile menuFile) {

        boolean updated = false;
        for (Map<String, Object> menu : menuList) {
            if (Integer.parseInt(String.valueOf(menu.get("menuIdx"))) == menuIdx) {
                menu.put("menuName", menuName);
                menu.put("menuPrice", menuPrice);
                menu.put("menuDescription", menuDescription);
                menu.put("categoryIdx", categoryIdx);
                if (menuFile != null && !menuFile.isEmpty()) {
                    menu.put("menuImg", "/uploads/" + menuFile.getOriginalFilename());
                }
                updated = true; break;
            }
        }
        return ResponseEntity.ok(Collections.singletonMap("success", updated));
    }

    /* ==========================================================
     * [SECTION 2] 핵심 업무 및 기타 (변수명 무결성 유지)
     * ========================================================== */

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("orderList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : orderList);
        model.addAttribute("storeVo", IS_EMPTY_DATA_TEST ? new HashMap<>() : storeData);
        model.addAttribute("menu", "dash");
        return "owner/owner-dashboard";
    }

    @GetMapping("/order/history")
    public String orderHistory(Model model) {
        model.addAttribute("orderList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : orderList);
        model.addAttribute("menu", "history");
        return "owner/owner-order-list";
    }

    @GetMapping("/category/setting")
    public String categorySetting(Model model) {
        model.addAttribute("categoryList", IS_EMPTY_DATA_TEST ? new ArrayList<>() : categoryList);
        model.addAttribute("menu", "category");
        return "owner/category-setting";
    }

    @PostMapping("/menu/toggle-soldout")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSoldOut(@RequestBody Map<String, Object> params) {
        String targetIdx = String.valueOf(params.get("menuIdx"));
        for (Map<String, Object> menu : menuList) {
            if (String.valueOf(menu.get("menuIdx")).equals(targetIdx)) {
                String current = String.valueOf(menu.get("menuStatus"));
                menu.put("menuStatus", "ON_SALE".equals(current) ? "SOLD_OUT" : "ON_SALE");
                break;
            }
        }
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    @PostMapping("/menu/delete")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> menuDelete(@RequestBody Map<String, Object> params) {
        String targetIdx = String.valueOf(params.get("menuIdx"));
        menuList = menuList.stream().filter(m -> !String.valueOf(m.get("menuIdx")).equals(targetIdx)).collect(Collectors.toList());
        return ResponseEntity.ok(Collections.singletonMap("success", true));
    }

    // 기타 사이드바 매핑 (누락 없음)
    @GetMapping("/menu/register") public String menuRegister(Model model) { model.addAttribute("categoryList", categoryList); model.addAttribute("menu", "menu_reg"); return "owner/owner-menu-register"; }
    @GetMapping("/store/setting") public String storeSetting(Model model) { model.addAttribute("storeVo", storeData); model.addAttribute("menu", "store"); return "owner/store-setting"; }
    @GetMapping("/review/review-management") public String reviewManagement(Model model) { model.addAttribute("menu", "review"); return "owner/review-management"; }
    @GetMapping("/sales/report") public String salesReport(Model model) { model.addAttribute("menu", "report"); return "owner/sales-report"; }
    @GetMapping("/support/notice") public String supportNotice(Model model) { model.addAttribute("menu", "notice"); return "owner/owner-dashboard"; }
    @GetMapping("/settlement/info") public String settlementInfo(Model model) { model.addAttribute("menu", "settle"); return "owner/owner-dashboard"; }

    /* ==========================================================
     * [UTIL]
     * ========================================================== */
    private static Map<String, Object> createOrder(int idx, String code, String date, String menu, int price, String status, String addr) {
        Map<String, Object> o = new HashMap<>();
        o.put("roomIdx", idx); o.put("orderCode", code); o.put("orderDate", date);
        o.put("menuName", menu); o.put("totalPrice", price); o.put("roomStatus", status);
        o.put("deliveryAddress1", addr);
        o.put("menuImg", "https://images.unsplash.com/photo-1626082927389-6cd097cdc6ec?w=200");
        return o;
    }

    private static Map<String, Object> createMenu(int idx, String name, int price, String cat, String status, String desc, int catIdx) {
        Map<String, Object> m = new HashMap<>();
        m.put("menuIdx", idx); m.put("menuName", name); m.put("menuPrice", price);
        m.put("categoryName", cat); m.put("menuStatus", status); m.put("menuDescription", desc);
        m.put("categoryIdx", catIdx);
        m.put("menuImg", "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=200");
        return m;
    }

    private static Map<String, Object> createMap(Object... args) {
        Map<String, Object> m = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) m.put(args[i].toString(), args[i + 1]);
        return m;
    }
}