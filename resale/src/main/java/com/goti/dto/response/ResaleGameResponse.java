package com.goti.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

public record ResaleGameResponse(
	UUID gameId,
	LocalDate playDate,
	LocalTime startAt
) {
	public LocalDateTime getGameStartDateTime() {
		return LocalDateTime.of(playDate, startAt);
	}

	public LocalDateTime getResaleDeadline() {
		return getGameStartDateTime().plusHours(1);
	}

	public boolean isResaleAvailable() {
		return LocalDateTime.now().isBefore(getResaleDeadline());
	}
	
}
