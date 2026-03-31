package com.moeats.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.GroupOrderItem;

@Mapper
public interface GroupOrderItemMapper {
	List<GroupOrderItem> findAll();
	GroupOrderItem findByIdx(int orderItemIdx);
	List<GroupOrderItem> findByOrder(int orderIdx);
	List<GroupOrderItem> findByOrderMember(
		@Param("orderIdx") int orderIdx,
		@Param("memberIdx") int memberIdx);
	List<GroupOrderItem> findOrderMemberAmount(int orderIdx);
	int insert(GroupOrderItem groupOrderItem);
	int delete(int orderIdx);
}
