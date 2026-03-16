package com.goti.payment.service.dto;

import java.util.UUID;

public record OrderPaymentConfirmApiRequest(
	UUID userId,
	UUID paymentId,
	String pgTid
) {
}
