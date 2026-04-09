package com.moeats.controller.temp;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class LegacyController {
	@GetMapping({"/members/dashboard","/owners/dashboard-map","/owners","/owner"})
	public String forward1(HttpServletRequest request) {
		log.warn("해당 URI는 다음으로 교체해야 함 :\nGET "+request.getRequestURI()+"\n -> GET /owners/dashboard");
		return "forward:/owners/dashboard";
	}
	@GetMapping({"/owners/store/setting","/owner/store/setting"})
	public String forward2(HttpServletRequest request) {
		log.warn("해당 URI는 다음으로 교체해야 함 :\nGET "+request.getRequestURI()+"\n -> GET /owners/menu/edit");
		return "forward:/owners/menu/edit";
	}
	@GetMapping({"/owners/menu/register","/owners/menu/write","/owners/owner-menu-register","/owner/menu/register","/owner/menu/write","/owner/owner-menu-register"})
	public String forward3(HttpServletRequest request){
		log.warn("해당 URI는 다음으로 교체해야 함 :\nGET "+request.getRequestURI()+"\n -> GET /owners/menu/new");
		return "forward:/owners/menu/new";
	}
	@GetMapping({"/owners/menu/management","/owners/menu/manage","/owner/menu/management","/owner/menu/manage","/owner/menu"})
	public String forward4(HttpServletRequest request) {
		log.warn("해당 URI는 다음으로 교체해야 함 :\nGET "+request.getRequestURI()+"\n -> GET /owners/menu");
		return "forward:/owners/menu";
	}
	@PostMapping({"/owners/menu/register", "/owners/menu/write","/owner/menu/register", "/owner/menu/write"})
	public String forward5(HttpServletRequest request) {
		log.warn("해당 URI는 다음으로 교체해야 함 :\nPOST "+request.getRequestURI()+"\n -> POST /owners/menu");
		return "forward:/owners/menu";
	}
	@GetMapping({"/owners/category/setting", "/owners/category/manage", "/owners/category/list","/owner/category/setting", "/owner/category/manage", "/owner/category/list"})
	public String forward6(HttpServletRequest request) {
		log.warn("해당 URI는 다음으로 교체해야 함 :\nGET "+request.getRequestURI()+"\n -> GET /owners/category");
		return "forward:/owners/category";
	}
	@GetMapping({"/owners/category/register","/owner/category/register"})
	public String forward7(HttpServletRequest request) {
		log.warn("해당 URI는 다음으로 교체해야 함 :\nGET "+request.getRequestURI()+"\n -> GET /owners/category/write");
		return "forward:/owners/category/write";
	}
}
