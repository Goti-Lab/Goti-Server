package com.goti.user.util;

import com.goti.infra.constants.redis.RedisKey;
import com.goti.user.config.properties.RefreshCookieProperties;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieProvider {

	private final RefreshCookieProperties properties;

	public ResponseCookie createRefreshTokenCookie(String token) {
		return createCookie(
			properties.refresh().name(),
			token,
			properties.refresh().path(),
			RedisKey.REFRESH_TOKEN.getTtl().toSeconds()
		);
	}

	public ResponseCookie deleteRefreshTokenCookie() {
		return deleteCookie(
			properties.refresh().name(), properties.refresh().path()
		);
	}

	public ResponseCookie createCookie(String name, String value, String path, long maxAge) {
		return createBaseCookieBuilder(name, path)
			.value(value)
			.maxAge(maxAge)
			.build();
	}

	public ResponseCookie deleteCookie(String name, String path) {
		return createBaseCookieBuilder(name, path)
			.maxAge(0)
			.build();
	}

	private ResponseCookie.ResponseCookieBuilder createBaseCookieBuilder(String name, String path) {
		return ResponseCookie.from(name, "")
			.httpOnly(true)
			.secure(properties.secure())
			.path(path)
			.sameSite(properties.sameSite());
	}

}
