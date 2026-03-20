package com.goti.payment.infra;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.payment.dto.internal.SettlementCompletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResaleSettlementEventListener {

	private final ResaleOrderClient resaleOrderClient;

	// 정산 완료 알림 전송
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleSettlementCompleted(SettlementCompletedEvent event) {
		try {
			resaleOrderClient.completeSettlement(event.resaleOrderId());
		} catch (Exception e) {
			throw new CustomException(ErrorCode.RESALE_ESCROW_FAILED);
		}
	}
}
