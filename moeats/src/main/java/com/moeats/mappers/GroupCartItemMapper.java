package com.moeats.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.GroupCartItem;

@Mapper
public interface GroupCartItemMapper {
	List<GroupCartItem> findAll();
	GroupCartItem findByIdx(int cartItemIdx);
	List<GroupCartItem> findByRoom(int roomIdx);
	List<GroupCartItem> findByMember(int memberIdx);
	List<GroupCartItem> findRoomMember(
		@Param("roomIdx") int roomIdx,
		@Param("memberIdx") int memberIdx);
	// 멤버별 총액
	List<GroupCartItem> findRoomMemberAmount(int roomIdx);
	// 총액
	int findRoomAmount(int roomIdx);
	// 각각 메뉴가 얼마나 있는지, 얼마인지
	List<GroupCartItem> findRoomMenuAmount(int roomIdx);
	int insert(GroupCartItem groupCartItem);
	int update(GroupCartItem groupCartItem);
	int remove(int cartItemIdx);
}
