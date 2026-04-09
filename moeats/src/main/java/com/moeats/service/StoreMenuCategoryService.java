package com.moeats.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moeats.domain.Store;
import com.moeats.domain.StoreMenuCategory;
import com.moeats.mapper.StoreMapper;
import com.moeats.mapper.StoreMenuCategoryMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreMenuCategoryService {
	
	@Autowired
	private StoreMenuCategoryMapper storeMenuCategorymapper;
	
	@Autowired
	private StoreMapper storeMapper;
	
	public int getStoreIdxByMember(int memberIdx) {

		Store store = storeMapper.myStore(memberIdx);

		if (store == null) {
			throw new IllegalStateException("가게가 등록되지 않았습니다.");
		}

		return store.getStoreIdx();
	}
	
	// 등록
	public void createCategory(StoreMenuCategory category) {
		storeMenuCategorymapper.insertCategory(category);
	}

	// 조회
	public StoreMenuCategory getCategory(int menuCategoryIdx) {

		StoreMenuCategory category =
				storeMenuCategorymapper.getCategory(menuCategoryIdx);

		if (category == null) {
			throw new IllegalStateException("카테고리 없음 또는 권한 없음");
		}

		return category;
	}
	public List<StoreMenuCategory> getCategoryByStore(int storeIdx) {
		
		List<StoreMenuCategory> category = storeMenuCategorymapper.getCategoryByStore(storeIdx);
		
		return category;
	}

	// 수정
	public void updateCategory(StoreMenuCategory category) {

		int result = storeMenuCategorymapper.updateCategory(category);

		if (result == 0) {
			throw new IllegalStateException("수정 실패 (권한 없음 또는 데이터 없음)");
		}
	}

	// 삭제
	public void deleteCategory(int menuCategoryIdx, int storeIdx) {

		int result = storeMenuCategorymapper.deleteCategory(menuCategoryIdx, storeIdx);

		if (result == 0) {
			throw new IllegalStateException("삭제 실패 (권한 없음 또는 데이터 없음)");
		}
	}
}

