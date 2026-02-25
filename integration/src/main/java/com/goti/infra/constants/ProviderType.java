package com.goti.infra.constants;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.FieldValidationException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProviderType {
	NAVER("naver"),
	KAKAO("kakao"),
	GOOGLE("google");

	private final String value;

	@JsonCreator
	public static ProviderType from(String value) {
		return Arrays.stream(ProviderType.values())
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
