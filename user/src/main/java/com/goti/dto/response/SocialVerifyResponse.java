package com.goti.dto.response;

public record SocialVerifyResponse(
	boolean isRegistered,
	String socialVerifyToken
) {

	public static SocialVerifyResponse of(boolean isRegistered, String token) {
		return new SocialVerifyResponse(
			isRegistered, token
		);
	}
}
