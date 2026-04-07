package com.moeats.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moeats.domain.GroupCartItem;
import com.moeats.domain.OrderRoom;
import com.moeats.domain.StoreMenu;
import com.moeats.mapper.StoreMenuMapper;
import com.moeats.mappers.GroupCartItemMapper;
import com.moeats.mappers.GroupOrderMapper;
import com.moeats.mappers.OrderRoomMapper;

@Service
public class GroupCartItemService {
	@Autowired
	GroupCartItemMapper groupCartItemMapper;
	@Autowired
	StoreMenuMapper storeMenuMapper;
	@Autowired
	GroupOrderMapper groupOrderMapper;
	@Autowired
	OrderRoomMapper orderRoomMapper;
	
	public GroupCartItem findByIdx(int cartItemIdx) {
		return groupCartItemMapper.findByIdx(cartItemIdx);
	}
	public List<GroupCartItem> findByRoom(int roomIdx){
		return groupCartItemMapper.findByRoom(roomIdx);
	}
	public List<GroupCartItem> findByMember(int memberIdx){
		return groupCartItemMapper.findByMember(memberIdx);
	}
	public List<GroupCartItem> findRoomMember(int roomIdx,int memberIdx){
		return groupCartItemMapper.findRoomMember(roomIdx,memberIdx);
	}
	public List<GroupCartItem> findRoomMemberAmount(int roomIdx){
		return groupCartItemMapper.findRoomMemberAmount(roomIdx);
	}
	public int findRoomAmount(int roomIdx) {
		return groupCartItemMapper.findRoomAmount(roomIdx);
	}
	public List<GroupCartItem> findRoomMenuAmount(int roomIdx){
		return groupCartItemMapper.findRoomMenuAmount(roomIdx);
	}
	public int insert(GroupCartItem groupCartItem) {
		OrderRoom orderRoom = orderRoomMapper.findByIdx(groupCartItem.getRoomIdx());
		StoreMenu storeMenu = storeMenuMapper.findByMenuIdx(orderRoom.getStoreIdx(),groupCartItem.getMenuIdx());
		if( orderRoom==null || storeMenu==null || orderRoom.getStoreIdx()!=storeMenu.getStoreIdx() )
			return 0;
		return groupCartItemMapper.insert(groupCartItem);
	}
	public int update(GroupCartItem groupCartItem) {
		return groupCartItemMapper.update(groupCartItem);
	}
	public int remove(int cartItemIdx) {
		return groupCartItemMapper.remove(cartItemIdx);
	}
}
