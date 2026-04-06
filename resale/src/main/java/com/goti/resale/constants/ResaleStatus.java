package com.goti.resale.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResaleStatus {
	SCHEDULED("리셀 예정"),
	AVAILABLE("리셀 가능"),
	UNAVAILABLE("리셀 매진");

	private final String description;
}
