package com.goti.payment.service.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.payment.domain.entity.payment.EscrowAccountEntity;
import com.goti.payment.dto.internal.SettlementCompletedEvent;
import com.goti.payment.infra.MockResaleEscrowClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResaleEscrowService {

	private final MockResaleEscrowClient escrowClient;
	private final ApplicationEventPublisher eventPublisher;

	// 에스크로 생성
	@Transactional
	public void createEscrow(EscrowAccountEntity escrow) {
		String externalId = escrowClient.requestEscrowPayment(
			escrow.getTransactionId(),
			escrow.getEscrowAmount().longValue()
		);
		escrow.updateExternalId(externalId);
	}

	// 에스크로 정산 및 이벤트 발행
	@Transactional
	public void processSettlement(UUID orderId, List<EscrowAccountEntity> escrows) {

		for (EscrowAccountEntity escrow : escrows) {
			if (escrow.getExternalEscrowId() != null) {
				escrowClient.requestSettlement(escrow.getExternalEscrowId());
			}
			LocalDateTime releaseTime = LocalDateTime.now();
			escrow.settle(releaseTime);
		}

		eventPublisher.publishEvent(new SettlementCompletedEvent(orderId));
	}
}
