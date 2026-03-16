package com.goti.config.properties.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sms.api")
public record SmsProperties(
	boolean enabled,
	String key,
	String secret,
	String sender,
	String content,
	String domain
) {
}

