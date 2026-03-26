package com.moeats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/owner")
public class OwnerController {

    // [GET] 점주 대시보드 메인
    @GetMapping("/dashboard")
    public String ownerDashboard(Model model) {
        // 테스트를 위해 일단 false로 설정 (지도가 바로 뜨게 함)
        // 나중에 DB 연동 시: boolean isEmpty = menuService.getCount() == 0;
       boolean isEmpty = false;    // 지도연동
//       boolean isEmpty = true;     // 신규화면

        model.addAttribute("isEmpty", isEmpty);
        model.addAttribute("storeName", "Mo-Eats 대구 중앙점");
        return "owner/owner-dashboard";
    }

    // [GET] 새 메뉴 등록 페이지 이동
    @GetMapping("/menus/new")
    public String newMenuForm() {
        return "owner/owner-menu-register";
    }

    // [POST] 메뉴 일괄 등록 처리 (핵심 로직)
    @PostMapping("/menus/bulk")
    public String registerMenus(
            @RequestParam("names") List<String> names,
            @RequestParam("prices") List<Integer> prices,
            @RequestParam("contents") List<String> contents,
            @RequestParam("files") List<MultipartFile> files) {

        
        String uploadDir = System.getProperty("user.dir") + "/src/main/resources/static/uploads/menus/";
        
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs(); // 폴더가 없으면 생성

        try {
            for (int i = 0; i < names.size(); i++) {
                MultipartFile file = files.get(i);
                
                if (!file.isEmpty()) {
                    // 1. 파일명 중복 방지를 위한 UUID 생성
                    String originalFileName = file.getOriginalFilename();
                    String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
                    String savedFileName = UUID.randomUUID().toString() + extension;

                    // 2. 서버 로컬 폴더에 실제 파일 저장
                    File dest = new File(uploadDir + savedFileName);
                    file.transferTo(dest);

                    // 3. ------ 여기서 원래는 DB에 데이터를 저장
                    // 예: menuService.save(names.get(i), prices.get(i), contents.get(i), savedFileName);
                    System.out.println("저장 완료: " + names.get(i) + " -> " + savedFileName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/owner/menus/new?error"; // 실패 시 다시 등록창으로
        }

        // 모든 등록이 완료되면 대시보드로 이동!
        return "redirect:/owner/dashboard";
    }
    @GetMapping("/order/detail")
    public String orderDetail() {
        // 템플릿 경로: src/main/resources/templates/owner/owner-order-detail.html
        return "owner/owner-order-detail"; 
    }
 // 메뉴 관리 페이지 주소 설정
    @GetMapping("/menu/management")
    public String menuManagement() {
        // 실제 파일 위치: src/main/resources/templates/owner/menu-management.html
        return "owner/menu-management"; 
    }
    
    // OwnerController.java

    @GetMapping("/menu/register")
    public String menuRegister() {
        // templates/owner/menu-register.html 파일을 찾아서 보여줌
        return "owner/menu-register"; 
    }
}
    
    
