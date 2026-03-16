package com.goti.payment.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.goti.payment.domain.entity.payment.PaymentLedgerEntity;

public record ResalePaymentLedgerResponse(
	UUID id,
	UUID orderId,
	UUID paymentId,
	Integer totalAmount,
	Integer buyerFee,
	Integer sellerFee,
	Integer vat,
	Integer netProfit,
	Integer settlementAmount,
	Instant createdAt
) {
	public static ResalePaymentLedgerResponse from(PaymentLedgerEntity ledger) {
		return new ResalePaymentLedgerResponse(
			ledger.getId(),
			ledger.getOrderId(),
			ledger.getPaymentId(),
			ledger.getTotalAmount(),
			ledger.getBuyerFee(),
			ledger.getSellerFee(),
			ledger.getVat(),
			ledger.getNetProfit(),
			ledger.getSettlementAmount(),
			ledger.getCreatedAt()
		);
	}
}
