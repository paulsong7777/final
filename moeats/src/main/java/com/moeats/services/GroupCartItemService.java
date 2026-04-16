package com.moeats.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moeats.domain.RoomParticipant;
import com.moeats.mappers.RoomParticipantMapper;
import com.moeats.domain.GroupCartItem;
import com.moeats.domain.OrderRoom;
import com.moeats.domain.StoreMenu;
import com.moeats.mappers.GroupCartItemMapper;
import com.moeats.mappers.GroupOrderMapper;
import com.moeats.mappers.OrderRoomMapper;
import com.moeats.mappers.OrderStoreMenuMapper;

@Service
public class GroupCartItemService {
	@Autowired
	GroupCartItemMapper groupCartItemMapper;
	@Autowired
	OrderStoreMenuMapper storeMenuMapper;
	@Autowired
	GroupOrderMapper groupOrderMapper;
	@Autowired
	OrderRoomMapper orderRoomMapper;
	@Autowired
	RoomParticipantMapper roomParticipantMapper;
	
	
	private boolean isEditableRoomStatus(String roomStatus) {
	    return "OPEN".equals(roomStatus) || "SELECTING".equals(roomStatus);
	}

	private void beginSelectingIfNeeded(OrderRoom orderRoom) {
	    if (orderRoom == null) {
	        return;
	    }
	    if ("OPEN".equals(orderRoom.getRoomStatus())) {
	        orderRoomMapper.beginSelecting(orderRoom.getRoomIdx());
	        orderRoom.setRoomStatus("SELECTING");
	    }
	}
	
//	private void beginSelectingIfNeeded(OrderRoom orderRoom) {
//	    if (orderRoom == null) {
//	        return;
//	    }
//	    if ("OPEN".equals(orderRoom.getRoomStatus())) {
//	        orderRoomMapper.menuSelect(orderRoom);
//	        orderRoom.setRoomStatus("SELECTING");
//	    }
//	}

	private void markParticipantNotSelectedIfNeeded(int roomIdx, int memberIdx) {
	    RoomParticipant roomParticipant = roomParticipantMapper.findJoinedRoomMember(roomIdx, memberIdx);
	    if (roomParticipant == null) {
	        return;
	    }
	    if ("SELECTED".equals(roomParticipant.getSelectionStatus())) {
	        roomParticipantMapper.unselect(roomParticipant.getRoomParticipantIdx());
	    }
	}
	
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
	    StoreMenu storeMenu = storeMenuMapper.findByIdx(groupCartItem.getMenuIdx());

	    if (orderRoom == null || storeMenu == null || orderRoom.getStoreIdx() != storeMenu.getStoreIdx()) {
	        return 0;
	    }
	    if (orderRoom.isJoinLocked() || !isEditableRoomStatus(orderRoom.getRoomStatus())) {
	        return 0;
	    }

	    beginSelectingIfNeeded(orderRoom);
	    markParticipantNotSelectedIfNeeded(groupCartItem.getRoomIdx(), groupCartItem.getMemberIdx());

	    return groupCartItemMapper.insert(groupCartItem);
	}
	public int update(GroupCartItem groupCartItem) {
	    GroupCartItem saved = groupCartItemMapper.findByIdx(groupCartItem.getCartItemIdx());
	    if (saved == null) {
	        return 0;
	    }

	    OrderRoom orderRoom = orderRoomMapper.findByIdx(saved.getRoomIdx());
	    if (orderRoom == null || orderRoom.isJoinLocked() || !isEditableRoomStatus(orderRoom.getRoomStatus())) {
	        return 0;
	    }

	    beginSelectingIfNeeded(orderRoom);
	    markParticipantNotSelectedIfNeeded(saved.getRoomIdx(), saved.getMemberIdx());

	    groupCartItem.setRoomIdx(saved.getRoomIdx());
	    groupCartItem.setMemberIdx(saved.getMemberIdx());
	    groupCartItem.setMenuIdx(saved.getMenuIdx());

	    return groupCartItemMapper.update(groupCartItem);
	}
	public int remove(int cartItemIdx) {
	    GroupCartItem saved = groupCartItemMapper.findByIdx(cartItemIdx);
	    if (saved == null) {
	        return 0;
	    }

	    OrderRoom orderRoom = orderRoomMapper.findByIdx(saved.getRoomIdx());
	    if (orderRoom == null || orderRoom.isJoinLocked() || !isEditableRoomStatus(orderRoom.getRoomStatus())) {
	        return 0;
	    }

	    beginSelectingIfNeeded(orderRoom);
	    markParticipantNotSelectedIfNeeded(saved.getRoomIdx(), saved.getMemberIdx());

	    return groupCartItemMapper.remove(cartItemIdx);
	}
}
