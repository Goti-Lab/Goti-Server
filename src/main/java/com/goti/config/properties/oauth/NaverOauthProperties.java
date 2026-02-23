package com.goti.config.properties.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth.naver")
public record NaverOauthProperties(
	String clientId,
	String clientSecret,
	String redirectUri,
	String tokenUrl,
	String userInfoUrl
) {
}
