package com.goti.ticketing.infra.api.dto.response;

public record PaymentCancelResponse(
	String paymentStatus,
	String paymentMethod,
	String paymentType
) {
}
