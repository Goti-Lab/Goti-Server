package com.goti.resale.infra.dto;

import java.util.UUID;

public record TicketGameInfo(
	UUID gameId,
	UUID gradeId
) {
}
