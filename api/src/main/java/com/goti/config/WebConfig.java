package com.goti.config;

import com.goti.interceptor.AuthInterceptor;

import com.goti.user.constants.SecurityPathConstants;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final AuthInterceptor authInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor)
			.addPathPatterns(SecurityPathConstants.MEMBER_URLS)
			.excludePathPatterns("/api/v1/auth/logout")
			.order(1);
	}
}
