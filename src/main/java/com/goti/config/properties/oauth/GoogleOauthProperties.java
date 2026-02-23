package com.goti.config.properties.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.google")
public record GoogleOauthProperties(
	String clientId,
	String clientSecret,
	String redirectUri,
	String tokenUrl,
	String userInfoUrl
) {
}
