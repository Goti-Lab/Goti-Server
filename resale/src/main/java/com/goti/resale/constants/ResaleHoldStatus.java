package com.goti.resale.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResaleHoldStatus {
	HOLDING("점유 중"),
	RELEASED("점유 해제");

	private final String description;
}
