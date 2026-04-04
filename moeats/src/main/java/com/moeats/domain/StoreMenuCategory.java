package com.moeats.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreMenuCategory {
	
	int menuCategoryIdx;	// 가게 메뉴 카테고리 번호
	int storeIdx;					// 가게 번호
	String categoryName; 		// 카테고리명
	int displayOrder;				// 정렬순서?
}
