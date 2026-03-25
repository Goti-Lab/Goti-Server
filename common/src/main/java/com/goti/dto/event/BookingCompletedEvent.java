package com.goti.dto.event;

import java.util.UUID;

public record BookingCompletedEvent(
	UUID gameId,
	UUID memberId
) {
}
