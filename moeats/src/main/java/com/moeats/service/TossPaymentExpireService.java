package com.moeats.service;

import com.moeats.domain.PaymentTransaction;
import com.moeats.mapper.PaymentTransactionMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TossPaymentExpireService {

    private final PaymentTransactionMapper paymentTransactionMapper;
    private final TossPaymentCallbackService tossPaymentCallbackService;

    public TossPaymentExpireService(PaymentTransactionMapper paymentTransactionMapper,
                                    TossPaymentCallbackService tossPaymentCallbackService) {
        this.paymentTransactionMapper = paymentTransactionMapper;
        this.tossPaymentCallbackService = tossPaymentCallbackService;
    }

    // 60000 -> 5000 smoke test only
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void expireIndividualPayments() {
        List<Long> expiredPaymentIds = paymentTransactionMapper.selectExpiredIndividualPaymentIds();

        for (Long paymentIdx : expiredPaymentIds) {
            paymentTransactionMapper.markTransactionExpiredByPayment(paymentIdx);
            paymentTransactionMapper.markPendingSharesCancelled(paymentIdx);
            paymentTransactionMapper.updatePaymentCancelled(paymentIdx);
            paymentTransactionMapper.updateGroupOrderCancelledByPayment(paymentIdx);
            paymentTransactionMapper.updateOrderRoomExpiredByPayment(paymentIdx);

            List<PaymentTransaction> doneTransactions = paymentTransactionMapper.selectDoneTransactionsByPayment(paymentIdx);
            for (PaymentTransaction transaction : doneTransactions) {
                tossPaymentCallbackService.cancelOneDoneTransaction(transaction, "각자결제 만료로 자동 취소");
            }
        }
    }
}
