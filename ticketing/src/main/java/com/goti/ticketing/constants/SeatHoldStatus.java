package com.goti.ticketing.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeatHoldStatus {

	HOLDING("점유 중"),
	RELEASED("점유 해제");

	private final String description;
}
