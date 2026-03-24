package com.moeats.mappers;

import java.sql.Timestamp;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.moeats.domain.PaymentShare;

@Mapper
public interface PaymentShareMapper {
	List<PaymentShare> findAll();
	PaymentShare findByIdx(int paymentShareIdx);
	List<PaymentShare> findByPayment(int paymentIdx);
	List<PaymentShare> findByMember(int memberIdx);
	PaymentShare findByPaymentMember(
		@Param("paymentIdx") int paymentIdx,
		@Param("memberIdx") int memberIdx);
	// 결제하지 않은 사람을 찾는 메서드	- 사실상 결제 완료되었는지 판단하기 위한 메서드
	List<PaymentShare> findPaymentPending(int paymentIdx);
	// 만약 취소된다면 이미 결제한 사람을 찾아야한다
	List<PaymentShare> findPaymentPaidSelf(int paymentIdx);
	int insert(PaymentShare paymentShare);
	int paidByRepresentative(
		@Param("paymentShareIdx") int paymentShareIdx,
		@Param("paidAt") Timestamp paidAt);
	int paySelf(int paymentShareIdx);
	// 취소된다면 방 전체가 취소된다	- 따라서 paymentIdx를 받는다
	int cancel(int paymentIdx);
}