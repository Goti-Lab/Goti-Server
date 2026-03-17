package com.goti.resale.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResaleHoldStatus {
	HOLDING("점유 상태"),
	RELEASED("점유 해제");

	private final String description;
}
