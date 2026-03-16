package com.goti.constants;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.FieldValidationException;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum OAuthProvider {
	NAVER("naver"),
	KAKAO("kakao"),
	GOOGLE("google");

	private final String value;

	@JsonCreator
	public static OAuthProvider from(String value) {
		return Arrays.stream(OAuthProvider.values())
			.filter(type ->
				type.name().equalsIgnoreCase(value) ||
					type.value.equalsIgnoreCase(value)
			)
			.findFirst()
			.orElseThrow(
				() -> new FieldValidationException(ErrorCode.INVALID_PROVIDER_TYPE, value)
			);
	}
}
