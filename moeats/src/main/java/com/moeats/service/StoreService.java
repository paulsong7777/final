package com.moeats.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moeats.domain.DeliveryAddress;
import com.moeats.domain.Store;
import com.moeats.dto.StoreSearchCond;
import com.moeats.dto.StoreThumbnailDto;
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

    @Autowired
    private StoreThumbnailService storeThumbnailService;
    
    // 주문방 생성시 가게 정보를 불러오겠음.
    public Store getStoreByIdx(int storeIdx) {
        Store store = storeMapper.findByStoreIdx(storeIdx);

        if (store != null) {
            StoreThumbnailDto thumbnailDto = storeThumbnailService.getStoreThumbnail((long) storeIdx);
            if (thumbnailDto != null) {
                store.setHeroImageUrl(thumbnailDto.getStoreThumbnailUrl());
            }
        }

        return store;
    }
    
    
    
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

        if (!storeStatus.equals("ACTIVE")
                && !storeStatus.equals("INACTIVE")
                && !storeStatus.equals("PAUSED")) {
            throw new IllegalArgumentException("유효하지 않은 가게 상태값입니다: " + storeStatus);
        }

        Store existing = storeMapper.findByStoreIdxAndOwner(storeIdx, ownerMemberIdx);

        if (existing == null) {
            throw new RuntimeException("등록된 가게 없음");
        }

        storeMapper.updateStatus(storeIdx, ownerMemberIdx, storeStatus);
    }

    // 가게 정보 수정
    @Transactional
    public void updateStore(Store store) {

        Store existing = storeMapper.findByStoreIdxAndOwner(
            store.getStoreIdx(),
            store.getOwnerMemberIdx()
        );

        if (existing == null) {
            throw new RuntimeException("등록된 가게 없음");
        }

        validateCategory(store.getStoreCategory());
        storeMapper.updateStore(store);
    }

    // 가게 등록
    public void insertStore(Store store) {

        validateCategory(store.getStoreCategory());

        if (store.getStoreStatus() == null || store.getStoreStatus().isBlank()) {
            store.setStoreStatus("ACTIVE");
        }

        storeMapper.insertStore(store);
    }

    // 카테고리 검증
    private void validateCategory(String category) {
        if (category == null || !(category.equals("CHICKEN")
                || category.equals("PIZZA")
                || category.equals("CHINESE")
                || category.equals("KOREAN")
                || category.equals("CAFE"))) {
            throw new IllegalArgumentException("유효하지 않은 가게 카테고리입니다: " + category);
        }
    }

    // 내 가게 조회
    public Store myStore(int ownerMemberIdx) {
        return storeMapper.myStore(ownerMemberIdx);
    }
}