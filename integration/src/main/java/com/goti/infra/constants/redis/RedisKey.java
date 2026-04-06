package com.goti.infra.constants.redis;

import java.time.Duration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RedisKey {
	SMS_AUTH_CODE("auth:sms:", Duration.ofMinutes(3)),
	OAUTH_STATE("oauth:state:", Duration.ofMinutes(5)),
	TICKET_QR("ticket:qr-token:", Duration.ofMinutes(3)),
	REFRESH_TOKEN("auth:refresh-token:", Duration.ofDays(7)),
	MEMBER_IDENTITY_VERIFY("member:identity:verify:", Duration.ofMinutes(3)),
	QUEUE_SEQUENCE("queue:%s:sequence", null),
	QUEUE_WAITING("queue:%s:waiting", null),
	QUEUE_META("queue:%s:meta", null),
	QUEUE_ENTRY("queue:%s:entry:%s", Duration.ofMinutes(30)),
	QUEUE_ACTIVE_USERS("queue:%s:active-users", null),
	QUEUE_EXPIRATION_USERS("queue:expiration:users", null),
	RESERVATION_SESSION("ticketing:reservation-session:", Duration.ofMinutes(10));

	private final String prefix;

	private final Duration ttl;

	public String getKey(Object... vals) {
		if (prefix.contains("%s")) {
			return prefix.formatted(vals);
		}
		return prefix + vals[0];
	}
}
