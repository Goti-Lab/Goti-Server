package com.goti.resale.infra.dto;

import java.util.List;
import java.util.UUID;

import com.goti.resale.dto.request.ResaleTransactionItemRequest;

public record ResaleOrderCreatedEvent(
	UUID orderId,
	UUID buyerId,
	int totalBuyerAmount,
	int totalBuyerFee,
	int totalSellerFee,
	List<ResaleTransactionItemRequest> paymentItems,
	String idempotencyKey
) {
}
