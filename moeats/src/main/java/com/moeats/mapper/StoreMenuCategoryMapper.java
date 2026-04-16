package com.moeats.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.StoreMenuCategory;

@Mapper
public interface StoreMenuCategoryMapper {
	
	// 삭제
	int deleteCategory(@Param("menuCategoryIdx") int menuCategoryIdx,
					   @Param("storeIdx") int storeIdx);

	// 수정
	int updateCategory(StoreMenuCategory category);

	// 등록
	int insertCategory(StoreMenuCategory category);

    // 단건 조회
    StoreMenuCategory getCategory(@Param("menuCategoryIdx") int menuCategoryIdx);
    
    // 특정 가게의 전체 카테고리 조회
    List<StoreMenuCategory> getCategoryByStore(@Param("storeIdx") int storeIdx);
}
