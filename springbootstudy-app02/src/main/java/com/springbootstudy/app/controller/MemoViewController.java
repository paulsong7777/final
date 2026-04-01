package com.springbootstudy.app.controller;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.springbootstudy.app.service.MemoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/* Spring MVC Controller 클래스 임을 정의
 * @Controller 애노테이션이 적용된 클래스의 메서드는 기본적으로 뷰의 이름을 반환한다.
 **/
@Controller
@RequiredArgsConstructor
@Slf4j
public class MemoViewController {	
	
	// 클래스에 롬복의 @RequiredArgsConstructor가 적용되어 생성자를 통해 주입된다.	
	private final MemoService memoService;
		
	// @Controller 애노테이션이 적용된 클래스의 메서드는 뷰를 반환한다.
	@GetMapping("/thymeleaf1")
	public String thDefault01(Model model) {
		
		model.addAttribute("text1", "<h1>Hello Thymeleaf</h1>");
		model.addAttribute("memo", memoService.getMemo(2));
		model.addAttribute("today", LocalDate.now());
		
		// application.properties 파일에서 Thymeleaf 설정을 다음과 같이 설정했기 때문에 
		// spring.thymeleaf.prefix=classpath:/templates/ 
		// spring.thymeleaf.view-names=th/*
		// th/* 반환되는 뷰는 Thymeleaf가 적용되어 templates/th 폴더에서 뷰를 찾는다.
		return "th/default1";	
	}
	
	@GetMapping("/thymeleaf2/{no}")
	public String thDefault02(Model model, @PathVariable(name="no") int no) {		
		log.info("thDefault2 - no : " + no);
		
		model.addAttribute("mList", memoService.memoList());
		return "th/default2";	
	}

	@GetMapping("/thymeleaf3")	
	public String memoList(Model model, @RequestParam(value="no") int no) {
		log.info("thDefault3 - no : " + no);		
		
		model.addAttribute("memo", memoService.getMemo(no));
		model.addAttribute("score", 70);
		return "th/default3";
	}
	
	// @Controller 애노테이션이 적용된 클래스의 메서드는 뷰를 반환한다.
	@GetMapping("/memoListJsp")	
	public String memoListJSP(Model model) {
		
		// application.properties 파일에서 JSP 설정을 다음과 같이 설정했기 때문에 
		// spring.mvc.view.prefix=/WEB-INF/views/
		// spring.mvc.view.suffix=.jsp
		// th/ 접두어가 없는 뷰는 src/main/webapp/WEB-INF/views 폴더에서 뷰를 찾는다.
		model.addAttribute("mList", memoService.memoList());		
		return "main";
	}
	
	// 이거는 테스트용 - 교안에 작성하지 않음
	@GetMapping("/main")
	public String main(Model model) {		
		model.addAttribute("name", "홍길동");
		model.addAttribute("msg", "반갑습니다.");
		return "th/main";		
	}
}
