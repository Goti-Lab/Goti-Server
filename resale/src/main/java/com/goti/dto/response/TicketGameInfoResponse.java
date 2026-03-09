package com.goti.dto.response;

import java.util.UUID;

public record TicketGameInfoResponse(
	UUID gameId,
	UUID gradeId
) {
}