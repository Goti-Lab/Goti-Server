package com.goti.payment.service.application;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.payment.dto.response.ResalePaymentLedgerResponse;
import com.goti.payment.repository.PaymentLedgerRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentLedgerService {

	private final PaymentLedgerRepository ledgerRepository;

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
