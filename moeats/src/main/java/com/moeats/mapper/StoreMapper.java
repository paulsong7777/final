package com.moeats.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.Store;
import com.moeats.dto.StoreSearchCond;

@Mapper
public interface StoreMapper {
	
	// 고객 가게 조회
	public List<Store> storeList(StoreSearchCond cond);
	
	public Store findByStoreIdx(int storeIdx);
	
	// 가게 상태 수정
	public void updateStatus(@Param("storeIdx") int storeIdx, 
			@Param("ownerMemberIdx") int ownerMemberIdx,
		    @Param("storeStatus") String storeStatus);
	
	// 가게 정보 수정
	public void updateStore(Store store);
	
	// 가게 등록
	public void insertStore(Store store);
	
	// 가게 조회
	public Store myStore(int ownerMemberIdx);
}
