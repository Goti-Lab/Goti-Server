package com.goti.infra.constants.redis;

import java.time.Duration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisKey {
	SMS_AUTH_CODE("auth:sms:", Duration.ofMinutes(3)),
	OAUTH_STATE("oauth:state:", Duration.ofMinutes(5)),
	TICKET_QR("ticket:qr-token:", Duration.ofMinutes(3));

	private final String prefix;

	private final Duration ttl;

	public String getKey(Object val) {
		return prefix + val;
	}
}
