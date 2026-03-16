package com.goti.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.goti.config.jwt.JwtAccessDeniedHandler;
import com.goti.config.jwt.JwtAuthenticationEntryPoint;
import com.goti.config.jwt.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final JwtAuthenticationEntryPoint entryPoint;
	private final JwtAccessDeniedHandler accessDeniedHandler;

	public static final String[] PERMIT_PUBLIC_PATH = {
		"/api/v1/auth/**",
		"/api/v1/stadiums/**",
		"/api/v1/games/**",
		"/api/v1/resale/histories/**",
		"/api/v1/baseball-teams/**",
		"/actuator/**",
		"/swagger-ui/**",
		"/v3/api-docs/**"
	};

	public static final String[] PERMIT_MEMBER_PATH = {
		"/api/v1/resale/listings/**",
		"/api/v1/resale/holds/**",
		"/api/v1/resale/orders/**",
		"/api/v1/resale/payments/**",
		"/api/v1/orders/**"
	};

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable)
			.cors(Customizer.withDefaults())
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			).exceptionHandling(
				exceptions -> exceptions
					.authenticationEntryPoint(entryPoint)
					.accessDeniedHandler(accessDeniedHandler)
			).authorizeHttpRequests(
				auth -> auth
					.requestMatchers(PERMIT_PUBLIC_PATH).permitAll()
					.requestMatchers(PERMIT_MEMBER_PATH).hasRole("MEMBER")
					.anyRequest().authenticated()
			).addFilterBefore(
				jwtAuthenticationFilter,
				UsernamePasswordAuthenticationFilter.class
			);

		return http.build();
	}

}
