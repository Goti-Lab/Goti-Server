package com.goti.resale.constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ResaleAvailableStatus {
	ENABLED("활성"),
	DISABLED("비활성");

	private final String description;
}
