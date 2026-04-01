package com.springbootstudy.app.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springbootstudy.app.domain.Memo;
import com.springbootstudy.app.mapper.MemoMapper;

import lombok.extern.slf4j.Slf4j;

// MemoService 클래스가 서비스 계층의 스프링 빈(Bean) 임을 정의 
@Service
@Slf4j
public class MemoService {
	
	/* 이 클래스가 의존하는 MemoMapper 객체를 주입 받기 위한 애노테이션 설정
	 * 아래와 같이 프로퍼티에 @Autowired를 붙여 주입할 수도 있지만 생성자나
	 * setter 메서드를 만들고 @Autowired를 붙여 주입 받을 수도 있다.
	 **/
	@Autowired
	private MemoMapper memoMapper;
	
	public List<Memo> memoList() {
		log.info("service: memoList()");
		// return memoMapper.memoList();
		return memoMapper.findAll();
	}
	
	public Memo getMemo(int no) {	
		log.info("service: getMemo(int no)");
		return memoMapper.findByNo(no);
	}
	
	public void addMemo(Memo memo) {
		log.info("service: addMemo(Memo memo)");
		memoMapper.addMemo(memo);
	}
	
	public void updateMemo(Memo memo) {
		log.info("service: updateMemo(Memo memo)");
		memoMapper.updateMemo(memo);
	}
	
	public void deleteMemo(int no) {
		log.info("service: deleteMemo(int no)");
		memoMapper.deleteMemo(no);
	}
}
