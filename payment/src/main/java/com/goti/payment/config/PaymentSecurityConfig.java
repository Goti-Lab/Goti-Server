package com.goti.payment.config;

import com.goti.security.MeshSecuritySupport;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "spring.application.name", havingValue = "goti-payment-service")
public class PaymentSecurityConfig {

	@Bean
	public SecurityFilterChain paymentFilterChain(HttpSecurity http) throws Exception {
		MeshSecuritySupport.applyDefaults(http)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/actuator/**",
					"/swagger-ui/**",
					"/v3/api-docs/**"
				).permitAll()
				.anyRequest().authenticated()
			);

		return http.build();
	}
}
