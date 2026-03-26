package com.moeats.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.RoomParticipant;

@Mapper
public interface RoomParticipantMapper {
	List<RoomParticipant> findAll();
	RoomParticipant findByIdx(int roomParticipantIdx);
	List<RoomParticipant> findByRoom(int roomIdx);
	List<RoomParticipant> findByMember(int memberIdx);
	RoomParticipant findRoomMember(
		@Param("roomIdx") int roomIdx,
		@Param("memberIdx") int memberIdx);
	List<RoomParticipant> findUnpaid(int roomIdx);
	int insert(RoomParticipant roomParticipant);
	int setSelect(int roomParticipantIdx);
	int unselect(int roomParticipantIdx);
	int pay(int roomParticipantIdx);
	int leave(int roomParticipantIdx);
	int rejoin(int roomParticipantIdx);
}
