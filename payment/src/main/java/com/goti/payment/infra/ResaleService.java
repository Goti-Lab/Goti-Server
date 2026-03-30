package com.goti.payment.infra;

import java.util.List;
import java.util.UUID;

public interface ResaleService {
	void confirmOrder(UUID orderId, UUID paymentId);

	List<UUID> getTransactionIds(UUID orderId);
}
