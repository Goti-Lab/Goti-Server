package com.goti.stadium.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StadiumType {

	PRIMARY("제1구장"),
	SECONDARY("제2구장")
	;

	private final String description;
}
