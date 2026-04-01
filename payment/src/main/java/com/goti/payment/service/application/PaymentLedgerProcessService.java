package com.goti.payment.service.application;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.payment.dto.response.ResalePaymentLedgerResponse;
import com.goti.payment.dto.response.UnsettledAmountResponse;
import com.goti.payment.service.domain.EscrowAccountService;
import com.goti.payment.service.domain.PaymentLedgerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentLedgerProcessService {

	private final PaymentLedgerService paymentLedgerService;
	private final EscrowAccountService escrowAccountService;

	@Transactional(readOnly = true)
	public Page<ResalePaymentLedgerResponse> getLedgers(Pageable pageable) {
		return paymentLedgerService.findAll(pageable)
			.map(ResalePaymentLedgerResponse::from);
	}

	@Transactional(readOnly = true)
	public ResalePaymentLedgerResponse getLedgerByOrderId(UUID orderId) {
		return ResalePaymentLedgerResponse.from(paymentLedgerService.findByOrderId(orderId));
	}

	@Transactional(readOnly = true)
	public UnsettledAmountResponse getUnsettledAmounts(UUID sellerId) {
		Long totalAmount = escrowAccountService.sumUnsettledAmounts(sellerId);
		return new UnsettledAmountResponse(totalAmount);
	}
}
