package com.moeats.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.Delivery_address;

@Mapper
public interface DeliveryAddressMapper {
	
	
	// 기본 주소 off
	public void resetDefaultAddress(int memberIdx);
	
	// 선택 주소
	public void setDefaultAddress(@Param("member_idx") int memberIdx,
			@Param("delivery_address_idx") int deliveryAddressIdx);
	
	// 삭제
	public void deleteAddress(@Param("member_idx") int memberIdx,
			@Param("delivery_address_idx") int deliveryAddressIdx);
	
	// 수정
	public void updateAddress(Delivery_address deliveryAddress);
	
	// 등록
	public void insertAddress(Delivery_address deliveryAddress);
	
	// 1건 조회
	public Delivery_address addressByIdx(@Param("member_idx") int memberIdx,
			@Param("delivery_address_idx") int deliveryAddressIdx);
	
	// 주소 조회
	public List<Delivery_address> addressList(int memberIdx);
}
