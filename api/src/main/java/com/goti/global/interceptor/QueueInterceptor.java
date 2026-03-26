package com.goti.global.interceptor;

import com.goti.infra.cache.RedisCache;

import com.goti.infra.constants.redis.RedisKey;

import com.goti.user.security.ExtendedUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueInterceptor implements HandlerInterceptor {

	private final RedisCache redisCache;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		String tokenFromHeader = request.getHeader("X-Queue-Token");
		var pathVariables =
			(Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

		String gameId = pathVariables != null ? pathVariables.get("gameId") : null;
		UUID memberId = getCurrentMemberId();

		if (tokenFromHeader == null || gameId == null) {
			log.warn(
				"action=VALIDATE_TOKEN gameId={} userId={} result=FAIL reason=MISSING_DATA",
				gameId, memberId
			);
			throw new RuntimeException("검증 정보가 부족합니다.");
		}

		String userPassKey = RedisKey.QUEUE_PASSED.getKey(gameId, memberId);
		String cachedToken = redisCache.get(userPassKey, String.class);

		if (cachedToken == null || !cachedToken.equals(tokenFromHeader)) {
			log.warn(
				"action=VALIDATE_TOKEN gameId={} userId={} result=FAIL reason=INVALID_OR_EXPIRED_TOKEN",
				gameId, memberId
			);
			throw new RuntimeException("대기열 순서가 아니거나 세션이 만료되었습니다.");
		}

		log.info(
			"action=VALIDATE_TOKEN gameId={} userId={} result=SUCCESS",
			gameId, memberId
		);
		return true;
	}

	private UUID getCurrentMemberId() {

		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		// 인증 정보 자체가 없는 경우
		if (authentication == null) {
			log.error("action=AUTH_CHECK result=FAIL reason=NO_AUTHENTICATION");
			throw new RuntimeException("인증 정보가 없습니다.");
		}

		// Principal 타입이 맞지 않는 경우 (비로그인 혹은 다른 객체)
		if (!(authentication.getPrincipal() instanceof ExtendedUserDetails userDetails)) {
			log.error(
				"action=AUTH_CHECK result=FAIL reason=INVALID_PRINCIPAL_TYPE principal={}",
				authentication.getPrincipal()
			);
			throw new RuntimeException("유효하지 않은 인증 타입입니다.");
		}

		UUID userId = userDetails.getId();
		log.debug("action=AUTH_CHECK result=SUCCESS userId={}", userId);

		return userId;
	}
}
