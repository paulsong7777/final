package com.moeats.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moeats.domain.MenuImage;
import com.moeats.mapper.MenuImageMapper;

@Service
public class MenuImageService {
	
	@Autowired
	private MenuImageMapper menuImageMapper;
	
	// 이미지 삭제
	public void deleteImage(int menuImageIdx) {
		menuImageMapper.deleteImage(menuImageIdx);
	};
	
	
	// 대표 사진 변경
	@Transactional
	public void changePrimaryImage(int menuIdx, int menuImageIdx) {
		
		// 1. 기존 대표 사진 내리기
		menuImageMapper.resetPrimaryImage(menuIdx);
		
		// 2. 클릭한 사진 대표 사진으로
		menuImageMapper.setPrimaryImage(menuImageIdx, menuIdx);
	};
	
	
	// 이미지 수정
	public void updateImage(MenuImage menuImage) {
		menuImageMapper.updateImage(menuImage);
	};
	
	// 이미지 등록
	public void insertImage(MenuImage menuImage) {
		menuImageMapper.insertImage(menuImage);
	};
	
	
	// 메뉴 번호에 해당되는 이미지 리스트 조회
	public List<MenuImage> imageByIdx(int menuIdx){
		
		return menuImageMapper.imageByIdx(menuIdx);
	};
	
	// 메뉴 이미지 전체 조회
	public List<MenuImage> imageList(){
		
		return menuImageMapper.imageList();
	};
	
}
