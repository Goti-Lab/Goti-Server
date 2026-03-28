package com.goti.payment.dto.internal;

import java.util.UUID;

public record SettlementCompletedEvent(
	UUID resaleOrderId
) {
}
