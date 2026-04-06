package com.moeats.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.moeats.domain.DeliveryAddress;

@Mapper
public interface OrderDeliveryAddressMapper {
	List<DeliveryAddress> findAll();
	DeliveryAddress findByIdx(int deliveryAddressIdx);
	List<DeliveryAddress> findByMember(int memberIdx);
	int insert(DeliveryAddress deliveryAddress);
	int update(DeliveryAddress deliveryAddress);
	int inactivate(int deliveryAddressIdx);
}
