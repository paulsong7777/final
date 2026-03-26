package com.moeats.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moeats.domain.DeliveryAddress;
import com.moeats.mappers.DeliveryAddressMapper;
import com.moeats.mappers.MemberMapper;

@Service
public class MemberService {
	@Autowired
	MemberMapper memberMapper;
	@Autowired
	DeliveryAddressMapper deliveryAddressMapper;
	
	public DeliveryAddress findAddressByIdx(int deliveryAddressIdx) {
		return deliveryAddressMapper.findByIdx(deliveryAddressIdx);
	}
}
