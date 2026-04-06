package com.goti.user.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityPathConstants {

	public static final String[] PUBLIC_URLS = {
		"/api/v1/auth/**",
		"/api/v1/stadiums/**",
		"/api/v1/resales/histories/**",
		"/api/v1/resales/listings/games/**",
		"/api/v1/resales/games/**",
		"/api/v1/baseball-teams/**",
		"/api/v1/games/**",
		"/api/v1/seats/bulk",
		"/api/v1/stadium-seats/seat-sections",
		"/api/v1/stadium-seats/seat-grades",
		"/api/v1/game-seats/*/init",
		"/actuator/health",
		"/actuator/health/**",
		"/.well-known/**",
		"/api/v1/resales/listings/count",
		"/api/v1/payments/resales/unsettled",
	};

	public static final String[] MEMBER_URLS = {
		"/api/v1/resales/listings/**",
		"/api/v1/resales/holds/**",
		"/api/v1/resales/orders/**",
		"/api/v1/orders/**",
		"/api/v1/payments/**",
		"/api/v1/payments/resales/**",
		"/api/v1/seat-reservations/**",
		"/api/v1/seats/seat-sections/**",
		"/api/v1/stadium-seats/stadiums/**",
		"/api/v1/tickets/**",
		"/api/v1/game-seats/**",
		"/api/v1/teams/**",
		"/api/v1/members/**"
	};
}
