package com.moeats.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moeats.domain.DeliveryAddress;
import com.moeats.domain.Member;
import com.moeats.mappers.OrderDeliveryAddressMapper;
import com.moeats.mappers.OrderMemberMapper;

@Service("orderMemberService")
public class MemberService {
	@Autowired
	OrderMemberMapper memberMapper;
	@Autowired
	OrderDeliveryAddressMapper deliveryAddressMapper;
	
	public List<Member> findByIdxs(Collection<Integer> memberIdxs) {
		List<Member> members;
		if(memberIdxs.isEmpty())
			members = List.of();
		else if(memberIdxs.size()<10000)
			members = memberMapper.findByIdxs(new HashSet<Integer>(memberIdxs));
		else {
			members = new ArrayList<Member>();
			List<Integer> memberIdxList = new ArrayList<Integer>(memberIdxs);
			for(int i=0;i<memberIdxList.size();i+=10000)
				members.addAll(memberMapper.findByIdxs(new HashSet<Integer>(memberIdxList.subList(i, Math.min(i+10000,memberIdxList.size())))));
		}
		return members;
	}
	
	public DeliveryAddress findAddressByIdx(int deliveryAddressIdx) {
		return deliveryAddressMapper.findByIdx(deliveryAddressIdx);
	}
	public List<DeliveryAddress> findAddressByMember(int memberIdx) {
		return deliveryAddressMapper.findByMember(memberIdx);
	}
}
