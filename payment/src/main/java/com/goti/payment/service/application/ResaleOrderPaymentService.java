package com.goti.payment.service.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.payment.constants.PaymentStatus;
import com.goti.payment.domain.entity.payment.EscrowAccountEntity;
import com.goti.payment.dto.internal.SettlementCompletedEvent;
import com.goti.payment.dto.request.ResalePaymentRequest;
import com.goti.payment.dto.response.PaymentResponse;
import com.goti.payment.service.domain.EscrowAccountService;
import com.goti.payment.service.domain.PaymentLedgerService;
import com.goti.payment.service.domain.PaymentService;
import com.goti.payment.service.infra.ResaleService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResaleOrderPaymentService {
	private final PaymentService paymentService;
	private final PaymentLedgerService paymentLedgerService;
	private final EscrowAccountService escrowAccountService;
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
			paymentLedgerService.create(
				request.orderId(),
				payment.paymentId(),
				request.totalAmount(),
				request.totalBuyerFee(),
				request.totalSellerFee()
			);
			List<EscrowAccountEntity> escrows = escrowAccountService.createEscrows(request);

			escrowAccountService.requestEscrowPayments(escrows);

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

		List<EscrowAccountEntity> escrows = escrowAccountService.findAllByTransactionIds(transactionIds);

		List<EscrowAccountEntity> holdingEscrows = escrowAccountService.filterHoldings(escrows);

		if (holdingEscrows.isEmpty()) {
			return;
		}

		escrowAccountService.requestSettlements(holdingEscrows);

		escrowAccountService.settle(holdingEscrows, LocalDateTime.now());

		eventPublisher.publishEvent(new SettlementCompletedEvent(orderId));
	}
}
