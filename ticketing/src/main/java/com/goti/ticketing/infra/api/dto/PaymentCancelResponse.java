package com.goti.ticketing.infra.api.dto;

public record PaymentCancelResponse(
	String paymentStatus,
	String paymentMethod,
	String paymentType
) {
}
