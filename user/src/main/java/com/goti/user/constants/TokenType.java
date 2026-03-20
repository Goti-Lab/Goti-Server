package com.goti.user.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenType {

	ACCESS("AccessToken"),
	REFRESH("RefreshToken")
	;

	private final String description;
}
