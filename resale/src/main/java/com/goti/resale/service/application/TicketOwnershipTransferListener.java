package com.goti.resale.service.application;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.goti.resale.domain.entity.resale.ResaleOrderEntity;
import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;
import com.goti.resale.infra.TicketClient;
import com.goti.resale.infra.dto.TicketOwnershipTransferEvent;
import com.goti.resale.service.domain.ResaleOrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketOwnershipTransferListener {

	private final ResaleOrderService orderService;
	private final TicketClient ticketClient;

	@Async("ticketTransferExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleTicketOwnershipTransfer(TicketOwnershipTransferEvent event) {
		try {
			TransferResult result = transferAllOwnership(event);

			if (result.hasFailures()) {
				log.error("티켓 소유권 이전 일부 실패 - 주문ID: {}, 성공: {}, 실패: {}",
					event.resaleOrderId(), result.successCount(), result.failCount());
			}
		} catch (Exception e) {
			log.error("티켓 소유권 이전 처리 실패 - 주문ID: {}", event.resaleOrderId(), e);
		}
	}

	private TransferResult transferAllOwnership(TicketOwnershipTransferEvent event) {
		ResaleOrderEntity order = orderService.findOrderById(event.resaleOrderId());
		List<ResaleTransactionEntity> transactions = orderService.findTransactionByOrder(event.resaleOrderId());

		int successCount = 0;
		int failCount = 0;

		for (ResaleTransactionEntity transaction : transactions) {
			if (transferOwnership(transaction, order, event)) {
				successCount++;
			} else {
				failCount++;
			}
		}

		return new TransferResult(successCount, failCount);
	}

	private boolean transferOwnership(
		ResaleTransactionEntity transaction,
		ResaleOrderEntity order,
		TicketOwnershipTransferEvent event
	) {
		try {
			ticketClient.transferOwnership(
				transaction.getListing().getTicketId(),
				event.buyerId(),
				order.getBuyerNickname(),
				order.getBuyerEmail(),
				order.getBuyerPhone(),
				transaction.getId(),
				transaction.getTransactionPrice(),
				event.authToken()
			);
			return true;
		} catch (Exception e) {
			log.error("티켓 소유권 이전 실패 - 티켓ID: {}", transaction.getListing().getTicketId(), e);
			return false;
		}
	}

	private record TransferResult(int successCount, int failCount) {
		boolean hasFailures() {
			return failCount > 0;
		}
	}
}