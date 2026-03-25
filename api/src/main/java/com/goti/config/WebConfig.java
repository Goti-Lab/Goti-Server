package com.goti.config;

import com.goti.global.interceptor.QueueInterceptor;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	private final QueueInterceptor queueInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(queueInterceptor)
			.addPathPatterns("/api/v1/stadium-seats/stadiums/*/games/*/seat-grades")
			.excludePathPatterns("/api/v1/queue/**");
	}
}