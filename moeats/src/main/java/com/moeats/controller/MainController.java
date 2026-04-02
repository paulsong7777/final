package com.moeats.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {



	    @GetMapping("/")
	    public String index() {
	        // templates/index.html을 찾아 렌더링함
	        return "index"; 
	    }
	
}
