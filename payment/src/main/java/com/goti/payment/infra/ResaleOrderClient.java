package com.goti.payment.infra;

import java.util.List;
import java.util.UUID;

public interface ResaleOrderClient {
	void completeOrder(UUID orderId, UUID paymentId);

	List<UUID> getTransactionIds(UUID orderId);

	void completeSettlement(UUID orderId);
}
