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

	QUEUE_PENDING("queue:pending:", Duration.ofHours(3)),
	QUEUE_PASSED("queue:passed:", Duration.ofMinutes(10)),
	QUEUE_ACTIVE("queue:active:", Duration.ofSeconds(10))
	;
	private final String prefix;

	private final Duration ttl;
	private static final String COLON = ":";


	public String getKey(Object... params) {
		StringBuilder sb = new StringBuilder(prefix);
		for (Object param : params) {
			sb.append(param).append(COLON);
		}
		return sb.substring(0, sb.length() - 1);
	}

}
