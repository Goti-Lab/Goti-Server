package com.goti.payment.dto.response;

import java.util.List;
import java.util.UUID;

public record ResaleOrderListResponse(
	List<UUID> transactionIds
) {
}
