package com.moeats.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.Store;

@Mapper
public interface StoreMapper {
	
	public Store findByStoreIdxAndOwner(@Param("storeIdx") int storeIdx,
								@Param("ownerMemberIdx") int ownerMemberIdx);
	
	// 가게 상태 수정
	public void updateStatus(@Param("storeIdx") int storeIdx, 
			@Param("ownerMemberIdx") int ownerMemberIdx,
		    @Param("storeStatus") String storeStatus);
	
	// 가게 정보 수정
	public void updateStore(Store store);
	
	// 가게 등록
	public void insertStore(Store store);
	
	// 가게 조회
	public Store myStore(@Param("ownerMemberIdx") int ownerMemberIdx);
}
