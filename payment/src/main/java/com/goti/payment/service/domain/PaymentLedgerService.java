package com.goti.payment.service.domain;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.goti.payment.domain.entity.payment.PaymentLedgerEntity;

public interface PaymentLedgerService {
	PaymentLedgerEntity create(
		UUID orderId,
		UUID paymentId,
		Integer totalAmount,
		Integer buyerFee,
		Integer sellerFee
	);

	Page<PaymentLedgerEntity> findAll(Pageable pageable);

	PaymentLedgerEntity findByOrderId(UUID orderId);
}
