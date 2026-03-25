package com.goti.payment.service.domain;

import java.util.UUID;

import com.goti.payment.domain.entity.payment.PaymentLedgerEntity;

public interface PaymentLedgerDomainService {
	PaymentLedgerEntity create(
		UUID orderId,
		UUID paymentId,
		Integer totalAmount,
		Integer buyerFee,
		Integer sellerFee
	);
}
