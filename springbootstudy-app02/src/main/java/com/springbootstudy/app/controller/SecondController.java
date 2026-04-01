package com.springbootstudy.app.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springbootstudy.app.domain.Member;

/* Spring Rest Controller 클래스를 정의
 * @RestController는 RestAPI를 구현하는 컨트롤러를 지원하기 위한 애노테이션으로
 * 이 애노테이션이 붙은 클래스안에서 @RequestMapping, @GetMapping, @PostMapping 등의
 * 맵핑 애노테이션이 적용된 메서드에서 반환되는 값을 자동으로 JSON 형식의 문자열로
 * 변환해 주고 응답 본문에 추가해 클라이언트로 응답되도록 한다.
 **/
@RestController
//@Controller
public class SecondController {
	
	/* @GetMapping 애노테이션은 @RequestMapping 애노테이션을 확장한
	 * 애노테이션으로 HTTP GET 방식 요청을 처리하기 위한 URL 맵핑을
	 * 지원하는 애노테이션이다. 아래는 http://localhost:8080/ 으로
	 * 들어오는 GET 방식 요청을 처리하기 위한 URL 맵핑이다.     
	 **/
	@GetMapping("/")
	public String main(Model model) {
		/* Model은 요청을 처리한 결과 데이터를 의미하며 스프링에서는 화면에
		 * 출력하는 데이터를 뷰 페이지로 전달하기 위해서 Model 객체에 사용한다.
		 * Model에 데이터를 담을 때는 addAttribute(key, value) 메서드를 사용한다.
		 **/
		//model.addAttribute("name", "홍길동");
		//model.addAttribute("msg", "반갑습니다.");
		//return "main";
		return "{\"main\":\"여기는 메인\"}";
	}
	
	/* 스프링 컨트롤러 안에서 @RequestMapping, @GetMapping, @PostMapping 등의 
	 * 맵핑 애노테이션이 적용된 메서드의 파라미터와 반환 타입은 다음과 같다.
	 * 
	 * 1. Controller 메서드의 파라미터 타입으로 지정할 수 있는 객체와 애노테이션
	 * 
	 * - HttpServletRequest, HttpServletResponse
	 *   요청/응답을 처리하기 위한 서블릿 API
	 *   
	 * - HttpSession 
	 *   HTTP 세션을 위한 서블릿 API  
	 * 
	 * - org.springframework.ui.Model, ModelMap
	 *   뷰에 모델 데이터를 전달하기 위한 모델 객체
	 *   
	 * - 커맨드 객체(Domain, VO, DTO), java.util.Map
	 *   폼 또는 JSON 형식으로 들어오는 요청 데이터를 저장할 객체
	 * 
	 * - Errors, BindingResult    
	 *   검증 결과를 저장할 객체로 커맨드 객체 바로 뒤에 위치 시켜야 한다.
	 *   
	 * - @RequestParam
	 *   HTTP 요청 파라미터의 값을 메서드의 파라미터로 매핑하기 위한 애노테이션  
	 * 
	 * - @RequestHeader
	 *   HTTP 요청 헤더의 값을 파라미터로 받기 위한 애노테이션
	 *   
	 * - @RequestCookie
	 *   Cookie 데이터를 파라미터로 받기 위한 애노테이션
	 *   
	 * - @PathVariable
	 *   RESTful API 방식의 파라미터를 받기 위한 경로 변수 설정 애노테이션
	 *   
	 * - @RequestBody
	 *   요청 몸체의 데이터를 자바 객체로 변환하기 위한 애노테이션
	 *   String이나 JSON으로 넘어오는 요청 몸체의 데이터를 자바 객체로
	 *   변환하기 위한 사용하는 애노테이션 이다.
	 *   
	 * - Writer, OutputStream
	 *   응답 데이터를 직접 작성할 때 메서드의 파라미터로 지정해 사용한다.
	 *   
	 *   
	 * 2. Controller 메서드의 반환 타입으로 지정할 수 있는 객체와 애노테이션
	 * - String
	 *   뷰 이름을 반환할 때 메서드의 반환 타입으로 지정
	 * 
	 * - void
	 *   컨트롤러의 메서드에서 직접 응답 데이터를 작성할 경우 지정
	 * 
	 * - ModelAndView
	 *   모델과 뷰 정보를 함께 반환해야 할 경우 지정
	 *   이전의 컨트롤는 스프링이 지원한는 Controller 인터페이스를
	 *   구현해야 했는데 이때 많이 사용하던 반환 타입이다.
	 * 
	 * - 자바 객체 
	 *   메서드에 @ResponseBody가 적용된 경우나 메서드에서 반환되는
	 *   객체를 JSON 또는 XML과 같은 양식으로 응답을 변환 할 경우에 사용한다. 
	 **/
	
	/* 스프링은 클라이언트로부터 들어오는 요청 파라미터를 받을 수 있는 여러 가지
	 * 방법을 제공하고 있다. 아래와 같이 맵핑 애노테이션이 적용된 Controller
	 * 메서드의 파라미터 앞에 @RequestParam("요청 파라미터 이름")을 지정하면 
	 * 이 애노테이션이 앞에 붙은 매개변수에 요청 파라미터 값을 바인딩 시켜준다.
	 * 
	 * @RequestParam 애노테이션에 사용할 수 있는 속성은 아래와 같다.
	 * value : HTTP 요청 파라미터의 이름을 지정한다.
	 * required : 요청 파라미터가 필수인지 설정하는 속성으로 기본값은 true 이다.
	 * 			이 값이 true인 상태에서 요청 파라미터의 값이 존재하지 않으면
	 * 			스프링은 Bad Request(400) Exception을 발생시킨다.
	 * defaultValue : 요청 파라미터가 없을 경우 사용할 기본 값을 문자열로 지정한다.
	 * 
	 * @RequestParam(value="no", required=false, defaultValue="1")
	 * 
	 * @RequestParam 애노테이션은 요청 파라미터 값을 읽어와 Controller 메서드의
	 * 파라미터 타입에 맞게 변환해 준다. 만약 요청 파라미터를 Controller 메서드의 
	 * 파라미터 타입으로 변환할 수 없는 경우 스프링은 400 에러를 발생시킨다.
	 **/
	@GetMapping("/hello")	
	public Map<String, String> hello(@RequestParam("name") String name) {
		
		Map<String, String> map = new HashMap<>();		
		map.put("title", "Second Controller");
		map.put("greeting", "안녕 하세요 " + name + "님~");
		
		return map;
	}
	
	/* 경로 변수(@Path Variable) 사용
	 * 경로 변수는 URL 경로에서 변수를 사용해 클라이언트가 보낸 데이터를 추출하여
	 * 매개변수에 연결해주는 애노테이션이다. 경로 변수는 아래의 요청 맵핑에 사용한
	 * 경로에서 중괄호({id})로 둘러 쌓인 값을 의미하며 이 값을 @PathVariable("id")로
	 * 받아서 이 애노테이션이 적용된 메서드의 파라미터로 연결해주는 방식이다.
	 * 경로 변수는 값이 반드시 존재해야 하며 값이 없을 경우에 404 오류가 발생한다.
	 * 경로 변수는 주로 RestAPI를 구현할 때 데이터 조회, 수정, 삭제 등의 작업에서
	 * 리소스를 구별하기 위한 식별자로 많이 사용하며 여러 개를 사용할 수 있다.
	 **/
	@GetMapping("/members/{id}")
	public Map<String, Object> getMember(@PathVariable("id") String id) {
		Map<String, Object> map = new HashMap<>();
		map.put("id", id);
		map.put("name", "홍길동");
		map.put("age", 25);		
		
		return map;
	}
	
	/* 커맨드 객체 사용
	 * 스프링은 클라이언트로부터 전달된 파라미터를 객체로 처리 할 수 있는 아래와
	 * 같은 방법을 제공하고 있다. 아래와 같이 요청 파라미터를 전달받을 때 사용하는 
	 * 객체를 커맨드 객체라고 부르며 이 커맨드 객체는 자바빈 규약에 따라 프로퍼티에
	 * 대한 setter를 제공하도록 작성해야 한다. 그리고 파라미터 이름이 커맨드 객체의
	 * 프로퍼티와 동일하도록 폼 컨트롤의 name 이나 JSON의 프로퍼터를 맞춰야 한다.
	 * 
	 * 맵핑 애노테이션이 적용된 컨트롤러 메서드에 커맨드 객체를 파라미터로 지정하면
	 * 커맨드 객체의 프로퍼티와 동일한 이름을 가진 요청 파라미터의 데이터를 스프링이
	 * 자동으로 연결해 준다. 이때 스프링은 자바빈 규약에 따라 적절한 setter 메서드를
	 * 사용해 값을 설정한다.
	 * 
	 * 커맨드 객체의 프로퍼티와 일치하는 파라미터 이름이 없다면 값이 연결되지 않기
	 * 때문에 해당 프로퍼티는 기본 값을 가진다. 또한 프로퍼티의 데이터 형에 맞게 
	 * 적절히 형 변환을 하는데 만약 형 변환을 할 수 없는 경우 스프링은 BadRequest(400)
	 * 에러를 발생 시킨다. 예를 들면 커맨드 객체의 프로퍼티가 정수형 일 때 매칭 되는
	 * 값이 정수형으로 형 변환 할 수 없는 경우 400 에러를 발생 시킨다.
	 **/
	@PostMapping("/members")
	public Member addMember(Member member) {
	//public Member addMember(@RequestBody Member member) {
		
		return member;
	}	
}

