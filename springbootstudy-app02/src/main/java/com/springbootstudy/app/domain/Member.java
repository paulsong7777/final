package com.springbootstudy.app.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor			// 기본 생성자 자동 생성
@AllArgsConstructor			// 모든 프로퍼티를 받는 생성자 자동 생성
public class Member {
	private String id;
	private String name;
	private String age;
}
