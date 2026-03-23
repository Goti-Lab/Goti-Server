package com.goti.payment.service.domain;

import java.util.UUID;

import com.goti.payment.constants.PaymentMethod;
import com.goti.payment.dto.response.PaymentResponse;

public interface PaymentService {
	PaymentResponse create(
		UUID orderId,
		UUID userId,
		PaymentMethod paymentMethod,
		String idempotencyKey,
		Integer paymentAmount
	);

	PaymentResponse getByOrderId(UUID orderId);
}
