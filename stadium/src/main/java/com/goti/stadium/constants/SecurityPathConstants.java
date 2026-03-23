package com.goti.stadium.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityPathConstants {

	public static final String[] PUBLIC_URLS = {
		"/api/v1/stadiums/**",
		"/api/v1/baseball-teams/**",
	};
}
