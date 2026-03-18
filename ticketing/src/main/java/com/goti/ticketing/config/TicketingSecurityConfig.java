package com.goti.ticketing.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "spring.application.name", havingValue = "goti-ticketing-service")
public class TicketingSecurityConfig {

	@Bean
	public SecurityFilterChain ticketingFilterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(
					"/actuator/**",
					"/swagger-ui/**",
					"/v3/api-docs/**"
				).permitAll()
				.requestMatchers("/api/v1/games/**").permitAll()
				.anyRequest().permitAll() // TODO: JWT 인증 도입 시 orders/seats/tickets → authenticated()
			);

		return http.build();
	}
}
