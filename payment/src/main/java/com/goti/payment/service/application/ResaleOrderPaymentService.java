package com.goti.payment.service.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.payment.constants.PaymentStatus;
import com.goti.payment.domain.entity.payment.EscrowAccountEntity;
import com.goti.payment.domain.entity.payment.PaymentLedgerEntity;
import com.goti.payment.dto.internal.SettlementCompletedEvent;
import com.goti.payment.dto.request.ResalePaymentRequest;
import com.goti.payment.dto.response.PaymentResponse;
import com.goti.payment.infra.MockResaleEscrowClient;
import com.goti.payment.infra.ResaleOrderClient;
import com.goti.payment.repository.EscrowAccountRepository;
import com.goti.payment.repository.PaymentLedgerRepository;
import com.goti.payment.service.domain.PaymentLedgerDomainService;
import com.goti.payment.service.domain.PaymentService;
import com.goti.payment.service.domain.ResaleEscrowService;
import com.goti.payment.service.domain.ResaleOrderPaymentDomainService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResaleOrderPaymentService {
	private final ResaleOrderClient resaleOrderClient;
	private final PaymentService paymentService;
	private final PaymentLedgerDomainService paymentLedgerDomainService;
	private final PaymentLedgerRepository paymentLedgerRepository;
	private final ResaleEscrowService resaleEscrowService;
	private final MockResaleEscrowClient escrowClient;
	private final EscrowAccountRepository escrowAccountRepository;
	private final ResaleOrderPaymentDomainService domainService;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public PaymentResponse createResaleEscrow(ResalePaymentRequest request) {
		PaymentResponse payment = paymentService.create(
			request.orderId(),
			request.buyerId(),
			request.paymentMethod(),
			request.idempotencyKey(),
			request.totalAmount()
		);

		if (payment.paymentStatus() == PaymentStatus.SUCCESS) {
			PaymentLedgerEntity ledger = paymentLedgerDomainService.create(
				request.orderId(),
				payment.paymentId(),
				request.totalAmount(),
				request.totalBuyerFee(),
				request.totalSellerFee()
			);
			paymentLedgerRepository.save(ledger);

			List<EscrowAccountEntity> escrows = domainService.createEscrows(request);

			for (EscrowAccountEntity escrow : escrows) {
				String externalId = escrowClient.requestEscrowPayment(
					escrow.getTransactionId(),
					escrow.getEscrowAmount()
				);
				escrow.updateExternalId(externalId);
			}

			escrowAccountRepository.saveAll(escrows);

			confirmResalePayment(
				request.orderId(),
				request.buyerId(),
				payment.paymentId()
			);
		}

		return payment;
	}

	@Transactional
	public void releaseEscrow(UUID orderId) {

		List<UUID> transactionIds = getTransactionIds(orderId);

		List<EscrowAccountEntity> escrows = escrowAccountRepository.findAllByTransactionIdIn(transactionIds);

		List<EscrowAccountEntity> holdingEscrows = resaleEscrowService.filterHoldings(escrows);

		if (holdingEscrows.isEmpty()) {
			return;
		}

		for (EscrowAccountEntity escrow : holdingEscrows) {
			if (escrow.getExternalEscrowId() != null) {
				escrowClient.requestSettlement(escrow.getExternalEscrowId());
			}
		}

		resaleEscrowService.settle(holdingEscrows, LocalDateTime.now());
		escrowAccountRepository.saveAll(escrows);

		eventPublisher.publishEvent(new SettlementCompletedEvent(orderId));
	}

	public void confirmResalePayment(
		UUID orderId,
		UUID buyerId,
		UUID paymentId
	) {
		log.info("리셀 주문 결제 확정 - orderId: {}, buyerId: {}", orderId, buyerId);
		resaleOrderClient.completeOrder(orderId, paymentId);
	}

	public List<UUID> getTransactionIds(UUID orderId) {
		return resaleOrderClient.getTransactionIds(orderId);
	}
}
