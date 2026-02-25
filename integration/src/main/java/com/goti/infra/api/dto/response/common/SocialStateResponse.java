package com.goti.infra.api.dto.response.common;

public record SocialStateResponse(
	String state
) {
	public static SocialStateResponse of(String state) {
		return new SocialStateResponse(state);
	}
}

