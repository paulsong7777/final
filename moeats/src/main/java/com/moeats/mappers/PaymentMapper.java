package com.moeats.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.Payment;

@Mapper
public interface PaymentMapper {
	List<Payment> findAll();
	Payment findByIdx(int paymentIdx);
	List<Payment> findByOrder(int orderIdx);
	int insert(Payment payment);
	int payAmount(
		@Param("paymentIdx") int paymentIdx,
		@Param("paymentPaidAmount") int paymentPaidAmount);
	int progress(int paymentIdx);
	int pay(int paymentIdx);
	int cancel(int paymentIdx);
}
