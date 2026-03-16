package com.goti.config.sms;

import com.goti.config.properties.sms.SmsProperties;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.service.DefaultMessageService;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "sms.api", name = "enabled", havingValue = "true")
public class SmsConfig {

	@Bean
	public DefaultMessageService messageService(SmsProperties properties) {
		return NurigoApp.INSTANCE.initialize(
			properties.key(),
			properties.secret(),
			properties.domain()
		);
	}
}
