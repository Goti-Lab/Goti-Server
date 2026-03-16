package com.goti.service.dto;

import java.util.UUID;

public record OrderPaymentConfirmApiRequest(
	UUID userId,
	UUID paymentId,
	String pgTid
) {
}
