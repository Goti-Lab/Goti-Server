package com.goti.resale.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityPathConstants {

	public static final String[] PUBLIC_URLS = {
		"/api/v1/resales/histories/**",
		"/api/v1/resales/listings/games/**",
	};
}
