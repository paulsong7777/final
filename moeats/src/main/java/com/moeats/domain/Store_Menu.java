package com.moeats.domain;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Store_Menu {
	int menu_idx;				// 가게 메뉴 번호
	int store_idx;				// 가게 번호
	String menu_name;			// 메뉴이름
	String menu_description;	// 메뉴소개
	int menu_price;				// 메뉴가격
	String menu_status;			// 메뉴상태(구매가능, 품절 등)
	int display_order;		// 정렬순서
	Timestamp created_at;			// 생성시각
	Timestamp updated_at;			// 수정시각
}
