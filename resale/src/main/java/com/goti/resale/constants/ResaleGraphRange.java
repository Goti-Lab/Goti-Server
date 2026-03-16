package com.goti.resale.constants;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResaleGraphRange {
	HOUR("한 시간"),
	DAY("하루"),
	WEEK("일주일");

	private final String description;

	public Instant getTime() {
		return switch (this) {
			case HOUR -> Instant.now().minus(1, ChronoUnit.HOURS);
			case DAY -> Instant.now().minus(1, ChronoUnit.DAYS);
			case WEEK -> Instant.now().minus(7, ChronoUnit.DAYS);
		};
	}
}
