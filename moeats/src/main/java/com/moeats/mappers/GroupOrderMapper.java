package com.moeats.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.GroupOrder;

@Mapper
public interface GroupOrderMapper {
	List<GroupOrder> findAll();
	GroupOrder findByIdx(int orderIdx);
	List<GroupOrder> findActive();
	int insert(GroupOrder groupOrder);
	int pay(int orderIdx);
	int accept(int orderIdx);
	int prepare(int orderIdx);
	int deliver(int orderIdx);
	int ready(int orderIdx);
	int complete(int orderIdx);
	int cancel(int orderIdx);
	
	int checkIn(int orderIdx);
	int expectVisit(
		@Param("orderIdx") int orderIdx,
		@Param("expectedVisitAt") int expectedVisitAt);
}
