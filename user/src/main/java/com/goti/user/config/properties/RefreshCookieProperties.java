package com.goti.user.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cookie")
public record RefreshCookieProperties(
	boolean secure,
	String sameSite,
	Refresh refresh
) {
	public record Refresh(
		String name,
		String path
	) {}
}
