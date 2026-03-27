package com.goti.interceptor;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.infra.cache.RedisCache;

import com.goti.infra.constants.redis.RedisKey;
import com.goti.user.config.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

	private final RedisCache redisCache;
	private final JwtTokenProvider jwtTokenProvider;

	@Override
	public boolean preHandle(
		HttpServletRequest request, HttpServletResponse response, Object handler
	) {
		if (!(handler instanceof HandlerMethod)) {
			return true;
		}

		String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return true;
		}

		String accessToken = authHeader.substring(7);

		String blacklistKey = RedisKey.BLACKLIST.getKey(accessToken);

		if (redisCache.hasKey(blacklistKey)) {
			log.warn(
				"action=AUTH_CHECK result=FAIL reason=BLACKLISTED_TOKEN path={}, userId={}",
				request.getRequestURI(), jwtTokenProvider.extractSubject(accessToken)
			);
			throw new CustomException(ErrorCode.AUTH_LOGOUT_TOKEN);
		}

		return true;
	}
}
