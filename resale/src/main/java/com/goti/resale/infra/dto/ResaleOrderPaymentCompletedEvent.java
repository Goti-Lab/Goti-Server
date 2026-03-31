package com.goti.resale.infra.dto;

import java.util.UUID;

public record ResaleOrderPaymentCompletedEvent(
	UUID resaleOrderId,
	UUID buyerId,
	UUID paymentId
) {
}
