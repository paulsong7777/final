package com.moeats.services;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.moeats.domain.GroupCartItem;
import com.moeats.domain.GroupOrder;
import com.moeats.domain.Payment;
import com.moeats.domain.PaymentShare;
import com.moeats.domain.RoomParticipant;
import com.moeats.mappers.PaymentMapper;
import com.moeats.mappers.PaymentShareMapper;

@Service
public class PaymentService {
	@Autowired
	PaymentMapper paymentMapper;
	@Autowired
	PaymentShareMapper paymentShareMapper;
	
	public Payment findByIdx(int paymentIdx) {
	    return paymentMapper.findByIdx(paymentIdx);
	}
	
	public Payment findByOrder(int orderIdx) {
		return paymentMapper.findByOrder(orderIdx);
	}
	public int insert(Payment payment) {
		return paymentMapper.insert(payment);
	}
	
	public PaymentShare findShareByIdx(int paymentShareIdx) {
		return paymentShareMapper.findByIdx(paymentShareIdx);
	}
	public PaymentShare findPaymentMember(int paymentIdx,int memberIdx) {
		return paymentShareMapper.findPaymentMember(paymentIdx,memberIdx);
	}
	public List<PaymentShare> findByPayment(int paymentIdx) {
		return paymentShareMapper.findByPayment(paymentIdx);
	}
	public List<PaymentShare> findPaymentPending(int paymentIdx) {
		return paymentShareMapper.findPaymentPending(paymentIdx);
	}
	public List<PaymentShare> findPaymentPaidSelf(int paymentIdx) {
		return paymentShareMapper.findPaymentPaidSelf(paymentIdx);
	}
	public int insert(PaymentShare paymentShare) {
		return paymentShareMapper.insert(paymentShare);
	}
	public int paySelf(int paymentShareIdx) {
		return paymentShareMapper.paySelf(paymentShareIdx);
	}
	public int paidByRepresentative(int paymentIdx,Timestamp paidAt) {
		return paymentShareMapper.paidByRepresentative(paymentIdx,paidAt);
	}
	public int payAmount(int paymentIdx, int paymentPaidAmount) {
		return paymentMapper.payAmount(paymentIdx, paymentPaidAmount);
	}
	public int progress(int paymentIdx) {
		return paymentMapper.progress(paymentIdx);
	}
	public int pay(int paymentIdx) {
		return paymentMapper.pay(paymentIdx);
	}
	
	public int cancel(int paymentIdx) {
		int res = paymentShareMapper.cancel(paymentIdx);
		res += paymentMapper.cancel(paymentIdx);
		return res;
	}
	public int delete(int paymentIdx) {
		int res = paymentShareMapper.delete(paymentIdx);
		res += paymentMapper.delete(paymentIdx);
		return res;
	}
	
	public void setRepresentativePaymentShares(List<PaymentShare> paymentShares, Payment payment, List<RoomParticipant> roomParticipants, int representativeMemberIdx) {
		for(RoomParticipant roomParticipant : roomParticipants) {
			PaymentShare paymentShare = new PaymentShare();
			paymentShare.setPaymentIdx(payment.getPaymentIdx());
			paymentShare.setMemberIdx(roomParticipant.getMemberIdx());
			if(roomParticipant.getMemberIdx() == representativeMemberIdx)
				paymentShare.setShareAmount(payment.getPaymentRequestAmount());
			else
				paymentShare.setShareAmount(0);
			paymentShares.add(paymentShare);
		}
	}
	public void setIndividualPaymentShares(List<PaymentShare> paymentShares, Payment payment, List<GroupCartItem> groupCartItems) {
		for(GroupCartItem groupCartItem : groupCartItems) {
			PaymentShare paymentShare = new PaymentShare();
			paymentShare.setPaymentIdx(payment.getPaymentIdx());
			paymentShare.setMemberIdx(groupCartItem.getMemberIdx());
			paymentShare.setShareAmount(groupCartItem.getItemTotalAmount());
			paymentShares.add(paymentShare);
		}
	}
}
