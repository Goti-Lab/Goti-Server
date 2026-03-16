package com.goti.service.domain;

import java.util.UUID;

import com.goti.constants.PaymentMethod;
import com.goti.dto.response.PaymentResponse;

public interface PaymentService {
	PaymentResponse create(
		UUID orderId,
		UUID userId,
		PaymentMethod paymentMethod,
		String idempotencyKey,
		Integer paymentAmount
	);
}
