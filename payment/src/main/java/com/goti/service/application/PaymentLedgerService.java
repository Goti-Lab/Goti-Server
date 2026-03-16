package com.goti.service.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.payment.PaymentLedgerEntity;
import com.goti.dto.response.ResalePaymentLedgerResponse;
import com.goti.exception.CustomException;
import com.goti.repository.PaymentLedgerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentLedgerService {

	private final PaymentLedgerRepository ledgerRepository;

	@Transactional
	public void createLedger(
		UUID orderId,
		UUID paymentId,
		Integer totalAmount,
		Integer buyerFee,
		Integer sellerFee
	) {
		int totalFee = buyerFee + sellerFee;
		BigDecimal totalFeeBD = BigDecimal.valueOf(totalFee);
		BigDecimal vatBD = totalFeeBD
			.multiply(new BigDecimal("0.1"))
			.divide(new BigDecimal("1.1"), 0, RoundingMode.HALF_UP);

		int vat = vatBD.intValue();
		int netProfit = totalFee - vat;
		int settlementAmount = totalAmount - buyerFee - sellerFee;

		PaymentLedgerEntity ledger = PaymentLedgerEntity.create(
			orderId,
			paymentId,
			totalAmount,
			buyerFee,
			sellerFee,
			vat,
			netProfit,
			settlementAmount
		);
		ledgerRepository.save(ledger);
	}

	@Transactional(readOnly = true)
	public Page<ResalePaymentLedgerResponse> getLedgers(Pageable pageable) {
		return ledgerRepository.findAll(pageable)
			.map(ResalePaymentLedgerResponse::from);
	}

	@Transactional(readOnly = true)
	public ResalePaymentLedgerResponse getLedgerByOrderId(UUID orderId) {
		return ledgerRepository.findByOrderId(orderId)
			.map(ResalePaymentLedgerResponse::from)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
	}
}
