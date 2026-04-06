package com.goti.resale.service.application;

import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.resale.domain.entity.resale.ResaleOrderEntity;
import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;
import com.goti.resale.infra.TicketApiClient;
import com.goti.resale.infra.dto.TicketOwnershipTransferEvent;
import com.goti.resale.service.domain.ResaleOrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketOwnershipTransferListener {

	private final ResaleOrderService orderService;
	private final TicketApiClient ticketApiClient;

	@Async("ticketTransferExecutor")
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void handleTicketOwnershipTransfer(TicketOwnershipTransferEvent event) {

		try {
			ResaleOrderEntity orderEntity = orderService.findOrderById(event.resaleOrderId());

			List<ResaleTransactionEntity> transactions = orderService.findTransactionByOrder(
				event.resaleOrderId());

			int successCount = 0;
			int failCount = 0;

			for (ResaleTransactionEntity transaction : transactions) {
				try {
					transferOwnership(
						transaction,
						orderEntity,
						event.buyerId(),
						event.authToken()
					);
					successCount++;

				} catch (Exception e) {
					failCount++;
				}
			}

			if (failCount > 0) {
				log.error("⚠️ 일부 티켓 소유권 이전 실패 - 수동 처리 필요. 주문ID: {}",
					event.resaleOrderId());
			}

		} catch (Exception e) {
			log.error("❌ [2단계] 티켓 소유권 이전 처리 실패 - 주문ID: {}",
				event.resaleOrderId(), e);
		}
	}

	private void transferOwnership(
		ResaleTransactionEntity transaction,
		ResaleOrderEntity orderEntity,
		UUID buyerId,
		String authToken
	) {
		try {
			ticketApiClient.transferOwnership(
				transaction.getListing().getTicketId(),
				buyerId,
				orderEntity.getBuyerNickname(),
				orderEntity.getBuyerEmail(),
				orderEntity.getBuyerPhone(),
				transaction.getId(),
				transaction.getTransactionPrice(),
				authToken
			);

		} catch (Exception e) {
			log.error("❌ 티켓 소유권 이전 API 호출 실패 - 티켓ID: {}",
				transaction.getListing().getTicketId(), e);
			throw new CustomException(ErrorCode.TICKET_OWNERSHIP_TRANSFER_FAILED);
		}
	}
}