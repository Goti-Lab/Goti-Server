package com.goti.dto.internal;

import java.util.UUID;

public record ResaleOrderPaymentCompletedEvent(
	UUID resaleOrderId,
	UUID buyerId,
	UUID paymentId
) {
}
