package com.goti.resale.service.application;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
	private static final String TICKET_NUMBER_PREFIX = "RES-";
	private static final DateTimeFormatter TICKET_NUMBER_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMdd")
		.withZone(ZoneId.of("Asia/Seoul"));
	private static final int ORDER_SUFFIX_START_INDEX = 12;

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
			if (transferOwnership(transaction, order, event, successCount + 1)) {
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
		TicketOwnershipTransferEvent event,
		int sequence
	) {
		String ticketNumberPrefix = createPrefix(order.getCreatedAt(), order.getOrderNumber());
		try {
			ticketClient.transferOwnership(
				transaction.getListing().getTicketId(),
				event.buyerId(),
				order.getBuyerNickname(),
				order.getBuyerEmail(),
				order.getBuyerPhone(),
				transaction.getId(),
				transaction.getTransactionPrice(),
				generateTicketNumber(ticketNumberPrefix, sequence),
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

	public String createPrefix(Instant createdAt, String orderNumber) {
		return String.join("",
			TICKET_NUMBER_PREFIX,
			TICKET_NUMBER_DATE_FORMATTER.format(createdAt.atZone(ZoneId.of("Asia/Seoul"))),
			extractSuffix(orderNumber)
		);
	}

	private String extractSuffix(String orderNumber) {
		return Optional.ofNullable(orderNumber)
			.filter(s -> s.length() >= ORDER_SUFFIX_START_INDEX)
			.map(s -> s.substring(ORDER_SUFFIX_START_INDEX))
			.orElse("");
	}

	private String generateTicketNumber(String ticketNumberPrefix, int ticketSequence) {
		return ticketNumberPrefix + "-" + String.format("%03d", ticketSequence);
	}
}