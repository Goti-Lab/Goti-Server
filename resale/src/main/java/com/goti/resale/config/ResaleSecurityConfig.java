package com.goti.resale.config;

import static com.goti.security.MeshSecuritySupport.PUBLIC_PATHS;

import com.goti.security.MeshProperties;
import com.goti.security.MeshSecuritySupport;

import lombok.RequiredArgsConstructor;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.application.name", havingValue = "goti-resale-service")
public class ResaleSecurityConfig {

	private final MeshProperties meshProperties;

	@Bean
	public SecurityFilterChain resaleFilterChain(HttpSecurity http) throws Exception {
		MeshSecuritySupport.applyDefaults(http, meshProperties.enabled())
			.authorizeHttpRequests(auth -> {
				auth.requestMatchers(PUBLIC_PATHS).permitAll();
				auth.requestMatchers("/api/v1/resales/histories/**").permitAll();
				if (meshProperties.enabled()) {
					auth.anyRequest().authenticated();
				} else {
					auth.anyRequest().permitAll();
				}
			});

		return http.build();
	}
}
