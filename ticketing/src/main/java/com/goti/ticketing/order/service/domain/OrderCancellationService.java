package com.goti.ticketing.order.service.domain;

import java.util.UUID;

import com.goti.ticketing.constants.OrderCancellationRequestType;
import com.goti.ticketing.domain.entity.order.OrderCancellationEntity;
import com.goti.ticketing.domain.entity.order.OrderEntity;

public interface OrderCancellationService {
	OrderCancellationEntity create(
		OrderEntity order,
		OrderCancellationRequestType requestType,
		UUID requestedBy,
		Integer refundAmountTotal,
		Integer feeAmountTotal,
		String idempotencyKey
	);

	void validate(OrderCancellationEntity cancellation);

	void startRefund(OrderCancellationEntity cancellation);

	void complete(OrderCancellationEntity cancellation);
}
