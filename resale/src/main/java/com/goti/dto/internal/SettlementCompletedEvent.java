package com.goti.dto.internal;

import java.util.UUID;

public record SettlementCompletedEvent(
	UUID resaleOrderId
) {
}
