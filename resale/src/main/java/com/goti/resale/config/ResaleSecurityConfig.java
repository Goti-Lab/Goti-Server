package com.goti.resale.config;

import com.goti.security.MeshSecuritySupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@ConditionalOnProperty(name = "spring.application.name", havingValue = "goti-resale-service")
public class ResaleSecurityConfig {

	@Value("${goti.mesh.enabled:false}")
	private boolean meshEnabled;

	@Bean
	public SecurityFilterChain resaleFilterChain(HttpSecurity http) throws Exception {
		MeshSecuritySupport.applyDefaults(http, meshEnabled)
			.authorizeHttpRequests(auth -> {
				auth.requestMatchers(
					"/actuator/**",
					"/swagger-ui/**",
					"/v3/api-docs/**"
				).permitAll();
				auth.requestMatchers("/api/v1/resales/histories/**").permitAll();
				if (meshEnabled) {
					auth.anyRequest().authenticated();
				} else {
					auth.anyRequest().permitAll();
				}
			});

		return http.build();
	}
}
