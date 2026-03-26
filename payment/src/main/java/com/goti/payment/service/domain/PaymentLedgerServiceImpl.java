package com.goti.payment.service.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.payment.domain.entity.payment.PaymentLedgerEntity;

@Service
public class PaymentLedgerServiceImpl implements PaymentLedgerService {

	private static final BigDecimal VAT_RATE = new BigDecimal("0.1");
	private static final BigDecimal VAT_DIVISOR = new BigDecimal("1.1");

	@Override
	public PaymentLedgerEntity create(
		UUID orderId,
		UUID paymentId,
		Integer totalAmount,
		Integer buyerFee,
		Integer sellerFee
	) {
		int totalFee = buyerFee + sellerFee;
		BigDecimal totalFeeBD = BigDecimal.valueOf(totalFee);
		BigDecimal vatBD = totalFeeBD
			.multiply(VAT_RATE)
			.divide(VAT_DIVISOR, 0, RoundingMode.HALF_UP);

		int vat = vatBD.intValue();
		int netProfit = totalFee - vat;
		int settlementAmount = totalAmount - buyerFee - sellerFee;

		return PaymentLedgerEntity.create(
			orderId,
			paymentId,
			totalAmount,
			buyerFee,
			sellerFee,
			vat,
			netProfit,
			settlementAmount
		);
	}
}
