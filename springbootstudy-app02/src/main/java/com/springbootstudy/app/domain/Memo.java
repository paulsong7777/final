package com.springbootstudy.app.domain;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Memo {
	private int no;
	private String title;
	private String writer;
	private String content;
	private Date regDate;
}
