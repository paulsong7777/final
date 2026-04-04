package com.moeats.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moeats.domain.DeliveryAddress;
import com.moeats.domain.Store;
import com.moeats.dto.StoreSearchCond;
import com.moeats.mapper.DeliveryAddressMapper;
import com.moeats.mapper.MemberMapper;
import com.moeats.mapper.StoreMapper;

@Service
public class StoreService {

	@Autowired
	private StoreMapper storeMapper;
	
	@Autowired
	private DeliveryAddressMapper deliveryAddressMapper;

	@Autowired
    private MemberMapper memberMapper;
	
	// 고객 가게 리스트 조회 - 전체 or 카테고리 클릭 or 키워드 검색 - 
	public List<Store> getStoreList(StoreSearchCond cond, int memberIdx) {

	    // 1. 기본 배송지 ID 가져오기
	    Integer defaultAddrIdx = memberMapper.getDefaultAddressIdx(memberIdx);

	    if (defaultAddrIdx != null) {

	        // 2. 배송지 조회
	        DeliveryAddress addr = deliveryAddressMapper.addressByIdx(
	                memberIdx,
	                defaultAddrIdx
	        );
	        
	        // 기본 배송지 좌표값 등록
	        if (addr != null) {
	            cond.setUserLat(addr.getLatitude());
	            cond.setUserLng(addr.getLongitude());
	        }
	    }

	    // 3. 전체 카테고리 처리
	    if ("ALL".equals(cond.getCategory())) {
	        cond.setCategory(null);
	    }

	    // 4. keyword trim
	    if (cond.getKeyword() != null) {
	        cond.setKeyword(cond.getKeyword().trim());
	    }

	    return storeMapper.storeList(cond);
	}

	// 가게 상태 수정
	@Transactional
	public void updateStatus(int storeIdx, int ownerMemberIdx, String storeStatus) {
		// 1. 상태값 유효성 검사 (Java 단에서 방어)
	    if (!storeStatus.equals("ACTIVE") && !storeStatus.equals("INACTIVE") && !storeStatus.equals("PAUSED")) {
	        throw new IllegalArgumentException("유효하지 않은 가게 상태값입니다: " + storeStatus);
	    }
		// 가게 존재 여부 확인
		Store existing = storeMapper.findByStoreIdxAndOwner(storeIdx, ownerMemberIdx);
		
		if(existing == null) {
			throw new RuntimeException("등록된 가게 없음");
		}

		storeMapper.updateStatus(storeIdx, ownerMemberIdx, storeStatus);
	}
	
	// 가게 정보 수정
	@Transactional
	public void updateStore(Store store) {
		
		// 가게 존재 여부 확인
		Store existing = storeMapper.findByStoreIdxAndOwner(store.getStoreIdx(), store.getOwnerMemberIdx());
		
		if(existing == null) {
			throw new RuntimeException("등록된 가게 없음");
		}
		
		storeMapper.updateStore(store);
	}
	
	// 가게 등록
	public void insertStore(Store store) {
		// 카테고리 유효성 검사 (추가)
        validateCategory(store.getStoreCategory());
        
		storeMapper.insertStore(store);
	}
	
	// 카테고리 검증용 private 메서드 추가
    private void validateCategory(String category) {
        if (category == null || !(category.equals("CHICKEN") || category.equals("PIZZA") || 
            category.equals("CHINESE") || category.equals("KOREAN") || category.equals("CAFE"))) {
            throw new IllegalArgumentException("유효하지 않은 가게 카테고리입니다: " + category);
        }
    }
	
	// 내 가게 조회
	public Store myStore(int ownerMemberIdx) {
		
		return storeMapper.myStore(ownerMemberIdx);
	}
	
}
