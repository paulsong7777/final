package com.moeats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// static 이하의 파일은 controller로는 접속할 수 없으니 주의
// static은 다음 주소를 직접 쳐라: http://localhost:8080/templates/room/test/room-create.html
@Controller
public class TestMainController {

    @GetMapping("/")
    public String index() {
        // localhost:8080 접속 시 바로 /room/create 로 리다이렉트(토스) 시킵니다.
        return "redirect:/templates/room/test/room-create.html";
    }
}