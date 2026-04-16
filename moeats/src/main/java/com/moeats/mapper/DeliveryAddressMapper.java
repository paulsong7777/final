package com.moeats.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.DeliveryAddress;

@Mapper
public interface DeliveryAddressMapper {
	
	// 기본 주소 찾기
	public DeliveryAddress findDefaultAddress(int memberIdx);
	
	// 기본 주소 off
	public void resetDefaultAddress(int memberIdx);
	
	// 선택 주소
	public void setDefaultAddress(@Param("memberIdx") int memberIdx,
			@Param("deliveryAddressIdx") int deliveryAddressIdx);
	
	// 삭제
	public void deleteAddress(@Param("memberIdx") int memberIdx,
			@Param("deliveryAddressIdx") int deliveryAddressIdx);
	
	// 수정
	public void updateAddress(DeliveryAddress deliveryAddress);
	
	// 등록
	public void insertAddress(DeliveryAddress deliveryAddress);
	
	// 1건 조회
	public DeliveryAddress addressByIdx(@Param("memberIdx") int memberIdx,
			@Param("deliveryAddressIdx") int deliveryAddressIdx);
	
	// 주소 조회
	public List<DeliveryAddress> addressList(int memberIdx);
}
