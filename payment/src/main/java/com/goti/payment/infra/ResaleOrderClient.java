package com.goti.payment.infra;

import java.time.LocalDate;
import com.goti.payment.dto.response.ResalePurchaseListItemResponse;

import java.util.List;
import java.util.UUID;

public interface ResaleOrderClient {
	void completeOrder(UUID orderId, UUID paymentId);

	List<UUID> getTransactionIds(UUID orderId);

	void completeSettlement(UUID orderId);

	List<ResalePurchaseListItemResponse> getPurchases(
		UUID buyerId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	);
}
