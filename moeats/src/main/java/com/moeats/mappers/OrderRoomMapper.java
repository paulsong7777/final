package com.moeats.mappers;

import java.sql.Timestamp;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.OrderRoom;

@Mapper
public interface OrderRoomMapper {
	List<OrderRoom> findAll();
	OrderRoom findByIdx(int roomIdx);
	OrderRoom findByCode(String roomCode);
	List<OrderRoom> findByLeader(int leaderMemberIdx);
	List<OrderRoom> findExpired();
	List<OrderRoom> findOld();
	int insert(OrderRoom orderRoom);
	int menuSelect(OrderRoom orderRoom);
		// expiresAt은 Payment와의 정합성을 위해 Payment가 생성될 때 받아와서 업데이트 한다
	int paymentPend(
		@Param("roomIdx") int roomIdx,
		@Param("expiresAt") Timestamp expiresAt);
	int revertToSelect(int roomIdx);
	int confirm(int roomIdx);
	int cancel(int roomIdx);
	int expire(int roomIdx);
	int delete(int roomIdx);
	
	int expireAll();
	int deleteAll(List<OrderRoom> roomIdxList);
}
