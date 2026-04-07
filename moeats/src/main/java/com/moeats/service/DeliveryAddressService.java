package com.moeats.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moeats.domain.DeliveryAddress;
import com.moeats.geo.GeoPoint;
import com.moeats.geo.GeoService;
import com.moeats.mapper.DeliveryAddressMapper;

@Service
public class DeliveryAddressService {
	
	@Autowired
	private DeliveryAddressMapper deliveryAddressMapper;
	
	@Autowired
	private GeoService geoService;
	
	// 사용자 마이페이지 기본주소 조회
	public DeliveryAddress getDefaultAddress(int memberIdx) {
		return deliveryAddressMapper.findDefaultAddress(memberIdx);
	}
	
	
	// 기본 주소 ↔ 선택 주소 변경
    @Transactional
    public void changeDefaultAddress(int memberIdx, int deliveryAddressIdx) {

        // 1️. 주소 존재 여부 확인
        DeliveryAddress addr = deliveryAddressMapper.addressByIdx(memberIdx, deliveryAddressIdx);

        if (addr == null) {
            throw new RuntimeException("주소가 존재하지 않거나 권한 없음");
        }

        // 2. 기존 기본 주소 해제
        deliveryAddressMapper.resetDefaultAddress(memberIdx);

        // 3. 선택 주소 설정
        deliveryAddressMapper.setDefaultAddress(memberIdx, deliveryAddressIdx);
    }
	
	// 삭제
    @Transactional
    public void deleteAddress(int memberIdx, int deliveryAddressIdx) {

        DeliveryAddress addr = deliveryAddressMapper.addressByIdx(memberIdx, deliveryAddressIdx);

        if (addr == null) {
            throw new RuntimeException("주소 없음");
        }

        deliveryAddressMapper.deleteAddress(memberIdx, deliveryAddressIdx);

        // 👉 삭제한 게 기본 주소였으면
        if (addr.isActive()) {
            // 다른 주소 하나를 기본으로 설정
            List<DeliveryAddress> list = deliveryAddressMapper.addressList(memberIdx);

            if (!list.isEmpty()) {
                deliveryAddressMapper.setDefaultAddress(memberIdx, list.get(0).getDeliveryAddressIdx());
            }
        }
    }
	
	// 수정
    public void updateAddress(DeliveryAddress deliveryAddress) {

        // 1. 기존 주소 조회
        DeliveryAddress origin = deliveryAddressMapper.addressByIdx(
                deliveryAddress.getMemberIdx(),
                deliveryAddress.getDeliveryAddressIdx()
        );

        if (origin == null) {
            throw new RuntimeException("주소 없음");
        }

        // 2. 주소가 바뀐 경우만 좌표 재계산
        if (!origin.getDeliveryAddress1().equals(deliveryAddress.getDeliveryAddress1())) {

            GeoPoint point = geoService.getLatLng(deliveryAddress.getDeliveryAddress1());

            deliveryAddress.setLatitude(point.getLat());
            deliveryAddress.setLongitude(point.getLng());
        } else {
            // 주소 안 바뀌면 기존 좌표 유지 (중요)
            deliveryAddress.setLatitude(origin.getLatitude());
            deliveryAddress.setLongitude(origin.getLongitude());
        }

        // 3. 업데이트
        deliveryAddressMapper.updateAddress(deliveryAddress);
    }
	
	// 등록
	public void insertAddress(DeliveryAddress deliveryAddress) {
	    
		// 1. 주소 → 좌표 변환
	    GeoPoint point = geoService.getLatLng(deliveryAddress.getDeliveryAddress1());

	    deliveryAddress.setLatitude(point.getLat());
	    deliveryAddress.setLongitude(point.getLng());

	    // 2. DB 저장
		deliveryAddressMapper.insertAddress(deliveryAddress);
	}
	
	// 1건 조회
	public DeliveryAddress addressByIdx(int memberIdx, int deliveryAddressIdx) {
		
		return deliveryAddressMapper.addressByIdx(memberIdx, deliveryAddressIdx);
	};
	
	// 주소 조회
	public List<DeliveryAddress> getAddress(int memberIdx) {
		
		return deliveryAddressMapper.addressList(memberIdx);
	}
}
