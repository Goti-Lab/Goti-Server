package com.goti.ticketing.ticket.dto.response;

import java.util.UUID;

public record ResaleTicketGameInfoResponse(
	UUID gameId,
	UUID gradeId
) {
}
