package com.moeats.mappers;

import java.util.List;
import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.Member;

@Mapper
public interface MemberMapper {
	List<Member> findAll();
	Member findByIdx(int memberIdx);
	List<Member> findByIdxs(Set<Integer> memberIdxs);
	List<Member> findActive();
	int insert(Member member);
	int update(Member member);
	int setAddress(
		@Param("memberIdx") int memberIdx,
		@Param("defaultDeliveryAddressIdx") int defaultDeliveryAddressIdx);
	int inactivate(int memberIdx);
	int suspend(int memberIdx);
}
