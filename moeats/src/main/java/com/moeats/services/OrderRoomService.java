package com.moeats.services;

import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moeats.domain.OrderRoom;
import com.moeats.domain.RoomParticipant;
import com.moeats.mappers.OrderRoomMapper;
import com.moeats.mappers.RoomParticipantMapper;

@Service
public class OrderRoomService {
	/** 그룹과 멤버를 담당하는 서비스
	 */
	@Autowired
	OrderRoomMapper orderRoomMapper;
	@Autowired
	RoomParticipantMapper roomParticipantMapper;
	private final Random random = new Random();
	
	// 겹치지 않는 6자리 숫자와 알파벳으로 이루어진 코드 생성
	public String createCode() {
		String code;
		do {
			// -2118184960 = 36^6
//			int iCode = Integer.remainderUnsigned(random.nextInt(),-2118184960);
//			String sCode = "00000" + Integer.toUnsignedString(iCode,36).toUpperCase();
			String sCode = "00000" + random.nextInt(999999);
			code = sCode.substring(sCode.length() - 6);
		}while(findByCode(code) != null);
		return code;
	}
	public OrderRoom findActiveRoomByMember(int memberIdx) {
        return orderRoomMapper.findActiveRoomByMember(memberIdx);
    }
	
	public OrderRoom findByIdx(int roomIdx) {
		return orderRoomMapper.findByIdx(roomIdx);
	}
	public OrderRoom findByCode(String roomCode) {
		return orderRoomMapper.findByCode(roomCode);
	}
//	public List<OrderRoom> findByLeader(int leaderMemberIdx){
//		return orderRoomMapper.findByLeader(leaderMemberIdx);
//	}
//	public List<OrderRoom> findExpired(){
//		return orderRoomMapper.findExpired();
//	}
//	public List<OrderRoom> findOld(){
//		return orderRoomMapper.findOld();
//	}
	public int insert(OrderRoom orderRoom) {
		return orderRoomMapper.insert(orderRoom);
	}
	public int menuSelect(OrderRoom orderRoom) {
		if(!orderRoom.getRoomStatus().equals("OPEN"))
			return 0;
		return orderRoomMapper.menuSelect(orderRoom);
	}
	// 단독으로 사용되어선 안됨
	public int paymentPend(int roomIdx,Timestamp expiresAt) {
		OrderRoom orderRoom = findByIdx(roomIdx);
		if(!orderRoom.getRoomStatus().equals("SELECTING"))
			return 0;
		return orderRoomMapper.paymentPend(roomIdx, expiresAt);
	}
	public int revertToSelect(int roomIdx) {
		OrderRoom orderRoom = findByIdx(roomIdx);
		if(!orderRoom.getRoomStatus().equals("PAYMENT_PENDING"))
			return 0;
		return orderRoomMapper.revertToSelect(roomIdx);
	}
	public int confirm(int roomIdx) {
		OrderRoom orderRoom = findByIdx(roomIdx);
		List<RoomParticipant> unpaid = findUnpaid(roomIdx);
		if(!orderRoom.getRoomStatus().equals("PAYMENT_PENDING")||!unpaid.isEmpty())
			return 0;
		return orderRoomMapper.confirm(roomIdx);
	}
	public int cancel(int roomIdx) {
		OrderRoom orderRoom = findByIdx(roomIdx);
		if(!orderRoom.getRoomStatus().equals("PAYMENT_PENDING"))
			return 0;
		return orderRoomMapper.cancel(roomIdx);
	}
	public int expire(int roomIdx) {
		OrderRoom orderRoom = findByIdx(roomIdx);
		if(!orderRoom.getRoomStatus().equals("PAYMENT_PENDING"))
			return 0;
		return orderRoomMapper.expire(roomIdx);
	}
//	public int delete(int roomIdx) {
//		return orderRoomMapper.delete(roomIdx);
//	}
	
	public RoomParticipant findParticipantByIdx(int roomParticipantIdx) {
		return roomParticipantMapper.findByIdx(roomParticipantIdx);
	}
	public List<RoomParticipant> findByRoom(int roomIdx){
		return roomParticipantMapper.findByRoom(roomIdx);
	}
	public List<RoomParticipant> findParticipantByCode(String roomCode){
		OrderRoom orderRoom = findByCode(roomCode);
		int roomIdx = orderRoom!=null ? orderRoom.getRoomIdx() : 0;
		return roomParticipantMapper.findByRoom(roomIdx);
	}
//	public List<RoomParticipant> findByMember(int memberIdx){
//		return roomParticipantMapper.findByMember(memberIdx);
//	}
	public RoomParticipant findRoomMember(int roomIdx,int memberIdx) {
		return roomParticipantMapper.findRoomMember(roomIdx, memberIdx);
	}
	public List<RoomParticipant> findNotSelected(int roomIdx){
		return roomParticipantMapper.findNotSelected(roomIdx);
	}
	public List<RoomParticipant> findUnpaid(int roomIdx){
		return roomParticipantMapper.findUnpaid(roomIdx);
	}
	public int join(RoomParticipant roomParticipant) {
		if(findByIdx(roomParticipant.getRoomIdx()).isJoinLocked())
			return 0;
		RoomParticipant preExist = this.findRoomMember(roomParticipant.getRoomIdx(),roomParticipant.getMemberIdx());
		if(preExist==null)
			return roomParticipantMapper.insert(roomParticipant);
		int roomParticipantIdx = preExist.getRoomParticipantIdx();
		int result = roomParticipantMapper.rejoin(roomParticipantIdx);
		if(result==0)
			return 0;
		roomParticipant.setRoomParticipantIdx(roomParticipantIdx);
		roomParticipant.setLeftAt(null);
		roomParticipant.setJoinedAt(this.findParticipantByIdx(roomParticipantIdx).getJoinedAt());
		return result;
	}
	public int setSelect(int roomParticipantIdx) {
		OrderRoom orderRoom = findByIdx(findParticipantByIdx(roomParticipantIdx).getRoomIdx());
		if(!orderRoom.getRoomStatus().equals("SELECTING"))
			return 0;
		return roomParticipantMapper.setSelect(roomParticipantIdx);
	}
	public int unselect(int roomParticipantIdx) {
		OrderRoom orderRoom = findByIdx(findParticipantByIdx(roomParticipantIdx).getRoomIdx());
		if(!orderRoom.getRoomStatus().equals("SELECTING"))
			return 0;
		return roomParticipantMapper.unselect(roomParticipantIdx);
	}
	public int pay(int roomParticipantIdx) {
		OrderRoom orderRoom = findByIdx(findParticipantByIdx(roomParticipantIdx).getRoomIdx());
		if(!orderRoom.getRoomStatus().equals("PAYMENT_PENDING"))
			return 0;
		return roomParticipantMapper.pay(roomParticipantIdx);
	}
	public int leave(int roomParticipantIdx) {
		OrderRoom orderRoom = findByIdx(findParticipantByIdx(roomParticipantIdx).getRoomIdx());
		if(orderRoom.isJoinLocked())
			return 0;
		return roomParticipantMapper.leave(roomParticipantIdx);
	}
	
	// 활성 주문방 참가자 조회
	public RoomParticipant findJoinedRoomMember(int roomIdx, int memberIdx) {
	    return roomParticipantMapper.findJoinedRoomMember(roomIdx, memberIdx);
	}
	
	// 결제 전 방장 룸 폭파
	public int close(int roomIdx) {
	    OrderRoom orderRoom = findByIdx(roomIdx);
	    if (orderRoom == null) {
	        return 0;
	    }
	    if (!("OPEN".equals(orderRoom.getRoomStatus()) || "SELECTING".equals(orderRoom.getRoomStatus()))) {
	        return 0;
	    }
	    return orderRoomMapper.close(roomIdx);
	}
}
