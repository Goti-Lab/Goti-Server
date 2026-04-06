package com.goti.ticketing.infra.api;

import java.util.UUID;

import com.goti.ticketing.infra.api.dto.response.PaymentCancelResponse;
import com.goti.ticketing.infra.api.dto.response.UnsettledAmountResponse;

public interface TicketPaymentClient {
	PaymentCancelResponse cancelPayment(UUID orderId, UUID cancellationId);

	UnsettledAmountResponse getUnsettledAmounts(UUID sellerId);
}
