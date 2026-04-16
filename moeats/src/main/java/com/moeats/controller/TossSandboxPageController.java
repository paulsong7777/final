package com.moeats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TossSandboxPageController {

    @GetMapping("/sandbox/toss")
    public String sandboxPage() {
        return "home/toss-sandbox";
    }
}
