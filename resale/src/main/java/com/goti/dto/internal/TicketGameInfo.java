package com.goti.dto.internal;

import java.util.UUID;

public record TicketGameInfo(
	UUID gameId,
	UUID gradeId
) {
}