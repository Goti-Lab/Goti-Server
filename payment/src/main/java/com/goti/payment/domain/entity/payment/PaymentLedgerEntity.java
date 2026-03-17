package com.goti.payment.domain.entity.payment;

import static lombok.AccessLevel.*;

import java.util.UUID;

import com.goti.domain.base.ModificationTimestampEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "payment_ledgers")
@NoArgsConstructor(access = PROTECTED)
public class PaymentLedgerEntity extends ModificationTimestampEntity {

	@Column(name = "order_id", nullable = false)
	private UUID orderId;

	@Column(name = "payment_id", nullable = false)
	private UUID paymentId;

	@Column(name = "total_amount", nullable = false)
	private Integer totalAmount;

	@Column(name = "buyer_fee", nullable = false)
	private Integer buyerFee;

	@Column(name = "seller_fee", nullable = false)
	private Integer sellerFee;

	@Column(name = "vat", nullable = false)
	private Integer vat;

	@Column(name = "net_profit", nullable = false)
	private Integer netProfit;

	@Column(name = "settlement_amount", nullable = false)
	private Integer settlementAmount;

	private PaymentLedgerEntity(
		UUID orderId,
		UUID paymentId,
		Integer totalAmount,
		Integer buyerFee,
		Integer sellerFee,
		Integer vat,
		Integer netProfit,
		Integer settlementAmount
	) {
		this.orderId = orderId;
		this.paymentId = paymentId;
		this.totalAmount = totalAmount;
		this.buyerFee = buyerFee;
		this.sellerFee = sellerFee;
		this.vat = vat;
		this.netProfit = netProfit;
		this.settlementAmount = settlementAmount;
	}

	public static PaymentLedgerEntity create(
		final UUID orderId,
		final UUID paymentId,
		final Integer totalAmount,
		final Integer buyerFee,
		final Integer sellerFee,
		final Integer vat,
		final Integer netProfit,
		final Integer settlementAmount
	) {
		return new PaymentLedgerEntity(
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
