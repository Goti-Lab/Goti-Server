package com.goti.queue.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityPathConstants {

	public static final String[] PUBLIC_URLS = {
		"/actuator/prometheus",
		"/api/v1/queue/*/global-status",
		"/internal/**"
	};
}
