package com.goti.payment.service.application;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.payment.dto.response.ResalePaymentLedgerResponse;
import com.goti.payment.service.domain.LedgerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentLedgerService {

	private final LedgerService ledgerService;

	@Transactional(readOnly = true)
	public Page<ResalePaymentLedgerResponse> getLedgers(Pageable pageable) {
		return ledgerService.findAll(pageable)
			.map(ResalePaymentLedgerResponse::from);
	}

	@Transactional(readOnly = true)
	public ResalePaymentLedgerResponse getLedgerByOrderId(UUID orderId) {
		return ResalePaymentLedgerResponse.from(ledgerService.findByOrderId(orderId));
	}
}
