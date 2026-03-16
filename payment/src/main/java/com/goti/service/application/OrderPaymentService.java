package com.goti.service.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.PaymentMethod;
import com.goti.constants.PaymentStatus;
import com.goti.constants.messages.ErrorCode;
import com.goti.dto.response.PaymentResponse;
import com.goti.global.validation.Preconditions;
import com.goti.service.domain.PaymentService;
import com.goti.service.dto.PaymentOrderInfo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderPaymentService {
	private final PaymentOrderGateway paymentOrderGateway;
	private final PaymentService paymentService;

	@Transactional
	public PaymentResponse create(
		UUID orderId,
		UUID memberId,
		PaymentMethod paymentMethod,
		String idempotencyKey
	) {
		PaymentOrderInfo order = paymentOrderGateway.getPaymentOrder(orderId, memberId);

		Preconditions.validate(
			order.memberId().equals(memberId),
			ErrorCode.AUTH_PERMISSION_DENIED
		);

		PaymentResponse payment = paymentService.create(
			order.orderId(),
			memberId,
			paymentMethod,
			idempotencyKey,
			order.totalAmount()
		);

		if (payment.paymentStatus() == PaymentStatus.SUCCESS) {
			paymentOrderGateway.confirmPayment(
				orderId,
				memberId,
				payment.paymentId(),
				payment.pgTid()
			);
		}

		return payment;
	}
}
