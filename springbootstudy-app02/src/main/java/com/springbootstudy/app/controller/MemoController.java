package com.springbootstudy.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.springbootstudy.app.domain.Memo;
import com.springbootstudy.app.service.MemoService;

import lombok.RequiredArgsConstructor;

/* RestController 클래스 임을 정의
 * @RestController 애노테이션은 @Controller에 @ResponseBody가
 * 추가된 것과 동일하다. RestController의 주용도는 JSON으로 응답하는 것이다.  
 **/
@RestController
@RequiredArgsConstructor
public class MemoController {	
	
	// 클래스에 롬복의 @RequiredArgsConstructor가 적용되어 생성자를 통해 주입된다.
	private final MemoService memoService;
	
	/* 메모 리스트 요청 처리 메서드
	 * @RestController 애노테이션이 클래스에 적용되었기 때문에 
	 * 이 메서드에서 반환하는 값은 JSON으로 직렬화되어 응답 본문에 포함된다.
	 **/
	@GetMapping("/memos")
	public List<Memo> memoList() {
		return memoService.memoList();		
	}
	
	// no에 해당하는 메모 하나의 요청을 처리하는 메서드
	@GetMapping("/memos/{no}")
	public Memo getMemo(@PathVariable("no") int no) {
		return memoService.getMemo(no);
	}
	
	// 메모 추가 요청 처리 메서드
	@PostMapping("/memos")
	public Map<String, Object> addMemo(Memo memo) {
		memoService.addMemo(memo);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("result", true);
		resultMap.put("memo", memo);
		return resultMap;
	}
	
	/* 메모 수정 요청 처리 메서드	
	 * ResponseEntity 객체는 사용자 요청에 대한 응답 데이터를 포함하는 객체로
	 * HttpStatus, HttpHeaders, HttpBody를 포함하고 있어서 HTTP 상태
	 * 코드와 헤더 그리고 응답 본문에 포함되는 데이터를 제어할 수 있는 객체이다.
	 * 
	 * @RequestBody는 요청 본문으로 들어오는 JSON이나 XML 데이터를 Memo 객체로
	 * 변환한다. JSON이나 XML이 아니거나 포맷에 맞지 않으면 400 오류가 발생한다. 
	 **/
	@PutMapping("/memos")	
	public ResponseEntity<Memo> updateMemo(@RequestBody Memo memo) {
		memoService.updateMemo(memo);
		
		// HTTP 상태 코드를 200 OK로 설정하여 응답
		return ResponseEntity
					.status(HttpStatus.OK)
					.body(memoService.getMemo(memo.getNo()));
	}

	// 메모 삭제 요청 처리 메서드
	@DeleteMapping("/memos")
	public Map<String, Object> deleteMemo(@RequestParam("no") int no) {		
		memoService.deleteMemo(no);
		
		Map<String, Object> resultMap = new HashMap<String, Object>();		
		resultMap.put("result", true);		
		return resultMap;
	}	
}
