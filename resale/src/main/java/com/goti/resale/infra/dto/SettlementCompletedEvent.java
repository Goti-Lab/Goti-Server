package com.goti.resale.infra.dto;

import java.util.UUID;

public record SettlementCompletedEvent(
	UUID resaleOrderId
) {
}
