package com.moeats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/room")
public class RoomController {

	// 주문방 생성화면 테스트용
    @GetMapping("/create")
    public String createForm(Model model) {
        return "room/room-create";  // templates/room/room-create.html
    }

    // 참여페이지(초대받는사람) 테스트용
    @GetMapping("/join")
    public String joinForm(Model model) {
        return "room/room-join";
    }
    
    // 주문방 협업화면(방장 직행) 테스트용
    
}
