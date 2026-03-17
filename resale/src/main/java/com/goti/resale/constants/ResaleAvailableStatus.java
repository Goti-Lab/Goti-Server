package com.goti.resale.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResaleAvailableStatus {
	ENABLED("활성"),
	DISABLED("비활성");

	private final String description;
}
