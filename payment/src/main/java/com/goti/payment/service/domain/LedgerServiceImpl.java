package com.goti.payment.service.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.payment.domain.entity.payment.PaymentLedgerEntity;
import com.goti.payment.repository.PaymentLedgerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LedgerServiceImpl implements LedgerService {

	private final PaymentLedgerRepository ledgerRepository;

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

	@Override
	@Transactional
	public void save(PaymentLedgerEntity ledger) {
		ledgerRepository.save(ledger);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<PaymentLedgerEntity> findAll(Pageable pageable) {
		return ledgerRepository.findAll(pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public PaymentLedgerEntity findByOrderId(UUID orderId) {
		return ledgerRepository.findByOrderId(orderId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
	}
}
