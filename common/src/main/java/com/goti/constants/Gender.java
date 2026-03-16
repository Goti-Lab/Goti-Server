package com.goti.constants;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Gender {
	MALE("남"),
	FEMALE("여"),
	UNKNOWN("알수없음")
	;

	private final String description;

	public static Gender fromString(String value) {
		if (value == null || value.isBlank())
			return UNKNOWN;

		return switch (value.toLowerCase()) {
			case "m", "male" -> MALE;
			case "f", "female" -> FEMALE;
			default -> throw new CustomException(ErrorCode.INVALID_FORMAT, "성별");
		};
	}
}
