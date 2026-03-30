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
import com.goti.payment.service.domain.LedgerService;
import com.goti.payment.service.domain.PaymentService;
import com.goti.payment.service.domain.ResaleEscrowService;
import com.goti.payment.service.domain.ResaleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResaleOrderPaymentService {
	private final PaymentService paymentService;
	private final LedgerService ledgerService;
	private final ResaleEscrowService resaleEscrowService;
	private final ResaleService resaleService;
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
			PaymentLedgerEntity ledger = ledgerService.create(
				request.orderId(),
				payment.paymentId(),
				request.totalAmount(),
				request.totalBuyerFee(),
				request.totalSellerFee()
			);
			ledgerService.save(ledger);

			List<EscrowAccountEntity> escrows = resaleEscrowService.createEscrows(request);

			resaleEscrowService.requestEscrowPayments(escrows);

			resaleEscrowService.saveAll(escrows);

			resaleService.confirmOrder(
				request.orderId(),
				payment.paymentId()
			);
		}

		return payment;
	}

	@Transactional
	public void releaseEscrow(UUID orderId) {

		List<UUID> transactionIds = resaleService.getTransactionIds(orderId);

		List<EscrowAccountEntity> escrows = resaleEscrowService.findAllByTransactionIds(transactionIds);

		List<EscrowAccountEntity> holdingEscrows = resaleEscrowService.filterHoldings(escrows);

		if (holdingEscrows.isEmpty()) {
			return;
		}

		resaleEscrowService.requestSettlements(holdingEscrows);

		resaleEscrowService.settle(holdingEscrows, LocalDateTime.now());
		resaleEscrowService.saveAll(holdingEscrows);

		eventPublisher.publishEvent(new SettlementCompletedEvent(orderId));
	}
}
