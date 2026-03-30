package com.goti.payment.service.domain;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.goti.payment.domain.entity.payment.PaymentLedgerEntity;

public interface LedgerService {
	PaymentLedgerEntity create(
		UUID orderId,
		UUID paymentId,
		Integer totalAmount,
		Integer buyerFee,
		Integer sellerFee
	);

	void save(PaymentLedgerEntity ledger);

	Page<PaymentLedgerEntity> findAll(Pageable pageable);

	PaymentLedgerEntity findByOrderId(UUID orderId);
}
