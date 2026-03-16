package com.goti.resale.dto.internal;

import java.util.UUID;

public record TicketGameInfo(
	UUID gameId,
	UUID gradeId
) {
}
