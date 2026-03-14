package com.goti.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResaleEnabledStatus {

	ENABLED("가능"),
	DISABLED("불가능");

	private final String description;
}
