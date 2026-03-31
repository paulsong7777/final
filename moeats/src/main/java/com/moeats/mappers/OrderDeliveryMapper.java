package com.moeats.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.moeats.domain.OrderDelivery;

@Mapper
public interface OrderDeliveryMapper {
	List<OrderDelivery> findAll();
	OrderDelivery findByIdx(int orderDeliveryIdx);
	OrderDelivery findByOrder(int orderIdx);
	int insert(OrderDelivery orderDelivery);
	int delete(int orderIdx);
}
