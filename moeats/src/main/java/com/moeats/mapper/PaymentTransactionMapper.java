package com.moeats.mapper;

import com.moeats.domain.PaymentTransaction;
import com.moeats.domain.TossCheckoutTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PaymentTransactionMapper {

    TossCheckoutTarget selectRepresentativeTarget(@Param("paymentIdx") Long paymentIdx);

    TossCheckoutTarget selectIndividualTarget(@Param("paymentShareIdx") Long paymentShareIdx);

    Integer selectNextRepresentativeAttemptNo(@Param("paymentIdx") Long paymentIdx);

    Integer selectNextIndividualAttemptNo(@Param("paymentShareIdx") Long paymentShareIdx);

    int insertPaymentTransaction(PaymentTransaction paymentTransaction);

    PaymentTransaction selectByMerchantOrderId(@Param("merchantOrderId") String merchantOrderId);

    int markTransactionInProgress(@Param("paymentTransactionIdx") Long paymentTransactionIdx);

    int markTransactionDone(@Param("paymentTransactionIdx") Long paymentTransactionIdx,
                            @Param("providerPaymentKey") String providerPaymentKey,
                            @Param("providerMethod") String providerMethod,
                            @Param("approvedAmount") Integer approvedAmount,
                            @Param("rawResponse") String rawResponse);

    int markTransactionFailed(@Param("paymentTransactionIdx") Long paymentTransactionIdx,
                              @Param("failCode") String failCode,
                              @Param("failMessage") String failMessage,
                              @Param("rawResponse") String rawResponse);

    int markTransactionExpiredByPayment(@Param("paymentIdx") Long paymentIdx);

    int markTransactionCancelPending(@Param("paymentTransactionIdx") Long paymentTransactionIdx,
                                     @Param("failMessage") String failMessage);

    int markTransactionCancelled(@Param("paymentTransactionIdx") Long paymentTransactionIdx,
                                 @Param("cancelledAmount") Integer cancelledAmount,
                                 @Param("rawResponse") String rawResponse);

    int markAllSharesPaidByRepresentative(@Param("paymentIdx") Long paymentIdx);

    int markSharePaidSelf(@Param("paymentShareIdx") Long paymentShareIdx);

    int markPendingSharesCancelled(@Param("paymentIdx") Long paymentIdx);

    Integer countPendingShares(@Param("paymentIdx") Long paymentIdx);

    Integer selectPaidAmount(@Param("paymentIdx") Long paymentIdx);

    int markRoomParticipantsPaidByPayment(@Param("paymentIdx") Long paymentIdx);

    int markRoomParticipantPaidByShare(@Param("paymentShareIdx") Long paymentShareIdx);

    int updatePaymentProgress(@Param("paymentIdx") Long paymentIdx,
                              @Param("paymentStatus") String paymentStatus,
                              @Param("paidAmount") Integer paidAmount);

    int updatePaymentCancelled(@Param("paymentIdx") Long paymentIdx);

    int updateGroupOrderPaidByPayment(@Param("paymentIdx") Long paymentIdx);

    int updateGroupOrderCancelledByPayment(@Param("paymentIdx") Long paymentIdx);

    int updateOrderRoomConfirmedByPayment(@Param("paymentIdx") Long paymentIdx);

    int updateOrderRoomExpiredByPayment(@Param("paymentIdx") Long paymentIdx);

    List<Long> selectExpiredIndividualPaymentIds();

    List<PaymentTransaction> selectDoneTransactionsByPayment(@Param("paymentIdx") Long paymentIdx);
}
