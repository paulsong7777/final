package com.moeats.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.GroupOrder;

@Mapper
public interface GroupOrderMapper {
	List<GroupOrder> findAll();
	GroupOrder findByIdx(int orderIdx);
	GroupOrder findByRoom(int roomIdx);
	List<GroupOrder> findByStore(int storeIdx);
	List<GroupOrder> findActive();
	GroupOrder findLatestTrackableByMember(@Param("memberIdx") int memberIdx);
	int insert(GroupOrder groupOrder);
	int pay(int orderIdx);
	int accept(int orderIdx);
	int prepare(int orderIdx);
	int deliver(int orderIdx);
	int ready(int orderIdx);
	int complete(int orderIdx);
	int cancel(int orderIdx);
	int delete(int orderIdx);
	
	int checkIn(int orderIdx);
	int expectVisit(
		@Param("orderIdx") int orderIdx,
		@Param("expectedVisitAt") int expectedVisitAt);
	
	List<GroupOrder> findByMember(@Param("memberIdx") int memberIdx);
}
