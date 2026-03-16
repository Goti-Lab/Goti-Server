package com.goti.resale.dto.response;

import java.util.UUID;

public record TicketGameInfoResponse(
	UUID gameId,
	UUID gradeId
) {
}