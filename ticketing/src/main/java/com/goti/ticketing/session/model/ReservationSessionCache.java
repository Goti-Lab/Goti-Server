package com.goti.ticketing.session.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationSessionCache(
	UUID sessionId,
	LocalDateTime expiresAt
) {
}
