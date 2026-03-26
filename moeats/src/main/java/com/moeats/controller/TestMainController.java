package com.moeats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestMainController {

    @GetMapping("/")
    public String index() {
        // localhost:8080 접속 시 바로 /room/create 로 리다이렉트(토스) 시킵니다.
        return "redirect:/templates/room/room-create.html";
    }
}