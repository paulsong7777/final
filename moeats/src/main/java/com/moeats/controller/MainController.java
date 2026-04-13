package com.moeats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

import com.moeats.domain.Member;

@Controller
public class MainController {

    @RequestMapping({"/", "/main"})
    public String main(@SessionAttribute(name = "member", required = false) Member member) {

        if (member != null && "OWNER".equals(member.getMemberRoleType())) {
            return "redirect:/owners/dashboard";
        }

        return "member/main";
    }
}