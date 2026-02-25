package com.goti.infra.constants.redis;

import java.time.Duration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisKey {
	AUTH_CODE("auth:code:", Duration.ofMinutes(3)),
	OAUTH_STATE("oauth:state:", Duration.ofMinutes(5));

	private final String prefix;

	private final Duration ttl;

	public String getKey(Object val) {
		return prefix + val;
	}
}
