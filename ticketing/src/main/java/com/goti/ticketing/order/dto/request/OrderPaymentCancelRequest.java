package com.goti.ticketing.order.dto.request;

import java.util.UUID;

public record OrderPaymentCancelRequest(
	UUID cancellationId
) {
}
