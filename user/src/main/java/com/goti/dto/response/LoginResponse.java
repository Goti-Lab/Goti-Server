package com.goti.dto.response;

public record LoginResponse(
	boolean isRegistered,
	String accessToken,
	String registrationToken
) {

	public static LoginResponse authenticated(String accessToken) {
		return new LoginResponse(true, accessToken, null);
	}

	public static LoginResponse onboarding(String registrationToken) {
		return new LoginResponse(false, null, registrationToken);
	}
}
