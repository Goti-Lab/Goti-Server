package com.goti.payment.service.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.PaymentMethod;
import com.goti.constants.PaymentStatus;
import com.goti.payment.domain.entity.payment.EscrowAccountEntity;
import com.goti.payment.dto.request.ResalePaymentRequest;
import com.goti.payment.dto.response.PaymentResponse;
import com.goti.payment.repository.EscrowAccountRepository;
import com.goti.payment.service.domain.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleOrderPaymentService {
	private final PaymentOrderGateway paymentOrderGateway;
	private final PaymentService paymentService;
	private final PaymentLedgerService paymentLedgerService;
	private final EscrowAccountRepository escrowAccountRepository;

	@Transactional
	public PaymentResponse createResaleEscrow(ResalePaymentRequest request) {
		PaymentResponse payment = paymentService.create(
			request.orderId(),
			request.buyerId(),
			PaymentMethod.valueOf(request.paymentMethod()),
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
			escrowAccountRepository.saveAll(escrows);

			paymentOrderGateway.confirmResalePayment(
				request.orderId(),
				request.buyerId(),
				payment.paymentId()
			);
		}

		return payment;
	}

	@Transactional
	public void releaseEscrow(UUID orderId) {

		List<UUID> transactionIds = paymentOrderGateway.getTransactionIds(orderId);

		List<EscrowAccountEntity> escrows = escrowAccountRepository.findAllByTransactionIdIn(transactionIds);

		for (EscrowAccountEntity escrow : escrows) {
			LocalDateTime releaseTime = LocalDateTime.now();
			escrow.release(releaseTime);
			// TODO: 실제 정산 시 은행/PG API 호출 로직 추가
		}

		escrowAccountRepository.saveAll(escrows);
	}
}
