package com.moeats.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.MenuImage;

@Mapper
public interface MenuImageMapper {
	
	void deleteImagesByMenuIdx(int menuIdx);
	
	// Mapper 인터페이스와 Service에 각각 추가
	void clearPrimaryImage(int menuIdx);
	
	// 메뉴 사진 삭제
	public void deleteImage(int menuImageIdx);
	
	// 대표 사진 - 헤제
	public void resetPrimaryImage(int menuIdx);
	
	// 대표 사진 - 설정
	public void setPrimaryImage(@Param("menuImageIdx") int menuImageIdx,
					@Param("menuIdx") int menuIdx);
	
	// 이미지 수정
	public void updateImage(MenuImage menuImage);
	
	// 이미지 등록
	public void insertImage(MenuImage menuImage);
	
	// 메뉴번호에 해당하는 이미지 전체 조회 - 한메뉴에 여러개 사진 - List
	public List<MenuImage> imageByIdx(int menuIdx);
	
	// 메뉴 이미지 전체 조회
	public List<MenuImage> imageList(@Param("storeIdx") int storeIdx);
}
