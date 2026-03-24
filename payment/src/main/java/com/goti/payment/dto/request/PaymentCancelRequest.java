package com.goti.payment.dto.request;

import java.util.UUID;

public record PaymentCancelRequest(
	UUID cancellationId
) {
}
