package com.moeats.domain;

import java.sql.Timestamp;

<<<<<<< HEAD
import lombok.Data;

@Data
public class StoreMenu {
	int menuIdx;
	int storeIdx;
	String menuName;
	String menuDescription;
	int menuPrice;
	String menuStatus;
	int displayOrder;
	Timestamp createdAt;
	Timestamp updatedAt;
=======
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoreMenu {
	int menuIdx;						// 가게 메뉴 번호
	int storeIdx;						// 가게 번호
	String menuName;				// 메뉴이름
	String menuDescription;		// 메뉴소개
	int menuPrice;						// 메뉴가격
	String menuStatus;				// 메뉴상태(구매가능, 품절 등)
	int displayOrder;					// 정렬순서
	Timestamp createdAt;			// 생성시각
	Timestamp updatedAt;			// 수정시각
>>>>>>> origin/integration
}
