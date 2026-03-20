package com.goti.stadium.config;

import com.goti.security.MeshAuthenticationFilter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "spring.application.name", havingValue = "goti-stadium-service")
public class StadiumSecurityConfig {

	@Bean
	public SecurityFilterChain stadiumFilterChain(HttpSecurity http) throws Exception {
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
				.requestMatchers("/api/v1/stadiums/**").permitAll()
				.requestMatchers("/api/v1/baseball-teams/**").permitAll()
				.anyRequest().authenticated()
			)
			.addFilterBefore(
				new MeshAuthenticationFilter(),
				UsernamePasswordAuthenticationFilter.class
			);

		return http.build();
	}
}
