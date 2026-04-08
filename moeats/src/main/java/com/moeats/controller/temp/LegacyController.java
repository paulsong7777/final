package com.moeats.controller.temp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class LegacyController {
	@GetMapping({"/members/dashboard","/owners"})
	public String forward1() {
		log.warn("해당 URI는 다음으로 교체해야 함 : GET /owners/dashboard");
		return "forward:/owners/dashboard";
	}
	@GetMapping("/owner/store/setting")
	public String forward2() {
		log.warn("해당 URI는 다음으로 교체해야 함 : GET /owners/menu/edit");
		return "forward:/owners/menu/edit";
	}
	@GetMapping({"/owner/menu/register","/owner/menu/write","/owner/owner-menu-register"})
	public String forward3(){
		log.warn("해당 URI는 다음으로 교체해야 함 : GET /owners/menu/new");
		return "forward:/owners/menu/new";
	}
	@GetMapping({"/owner/menu/management","/owner/menu/manage"})
	public String forward4() {
		log.warn("해당 URI는 다음으로 교체해야 함 : GET /owners/menu");
		return "forward:/owners/menu";
	}
	@PostMapping({"/owner/menu/register", "/owner/menu/write"})
	public String forward5() {
		log.warn("해당 URI는 다음으로 교체해야 함 : POST /owners/menu");
		return "forward:/owners/menu";
	}
	@GetMapping({"/category/setting", "/category/manage", "/category/list"})
	public String forward6() {
		log.warn("해당 URI는 다음으로 교체해야 함 : GET /owners/store-menu-category");
		return "forward:/owners/store-menu-category";
	}
}
