package com.goti.payment.service.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.payment.constants.PaymentStatus;
import com.goti.payment.domain.entity.payment.EscrowAccountEntity;
import com.goti.payment.dto.request.ResalePaymentRequest;
import com.goti.payment.dto.response.PaymentResponse;
import com.goti.payment.infra.ResaleOrderClient;
import com.goti.payment.repository.EscrowAccountRepository;
import com.goti.payment.service.domain.PaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResaleOrderPaymentService {
	private final ResaleOrderClient resaleOrderClient;
	private final PaymentService paymentService;
	private final ResaleEscrowService escrowService;
	private final PaymentLedgerService paymentLedgerService;
	private final EscrowAccountRepository escrowAccountRepository;

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
			paymentLedgerService.createLedger(
				request.orderId(),
				payment.paymentId(),
				request.totalAmount(),
				request.totalBuyerFee(),
				request.totalSellerFee()
			);

			List<EscrowAccountEntity> escrows = request.items()
				.stream()
				.map(item ->
					EscrowAccountEntity
						.create(
							item.transactionId(),
							request.buyerId(),
							item.sellerId(),
							item.settlementAmount()
						))
				.toList();

			for (EscrowAccountEntity escrow : escrows) {
				escrowService.createEscrow(escrow);
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

		escrowService.processSettlement(orderId, escrows);
		escrowAccountRepository.saveAll(escrows);
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
