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
		// 1. 헤더 체크 (X-Queue-Token, X-Game-Id)
		String tokenFromHeader = request.getHeader("X-Queue-Token");
		var pathVariables =
			(Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

		String gameId = pathVariables != null ? pathVariables.get("gameId") : null;
		if (tokenFromHeader == null || gameId == null) {
			log.warn("검증 데이터 누락 - Token: {}, GameId: {}", tokenFromHeader, gameId);
			throw new RuntimeException("검증 정보가 부족합니다.");
		}

		// 2. 유저 ID 추출
		UUID memberId = getCurrentMemberId();
		log.info("memberId :: {}", memberId);

		// 3. Redis에서 통과된 유저인지 확인
		// Key: queue:passed:{gameId}:{memberId}
		String userPassKey = RedisKey.QUEUE_PASSED.getKey(gameId, memberId);
		String cachedToken = redisCache.get(userPassKey, String.class);

		// 4. 검증
		if (cachedToken == null || !cachedToken.equals(tokenFromHeader)) {
			log.warn("검증 실패 - 유저: {}, 게임: {}", memberId, gameId);
			throw new RuntimeException("대기열 순서가 아니거나 세션이 만료되었습니다.");
		}

		return true; // 통과!
	}

	private UUID getCurrentMemberId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof ExtendedUserDetails userDetails)) {
			log.error("인증 정보가 없거나 타입이 일치하지 않습니다.");
			throw new RuntimeException("인증 정보가 없습니다."); // CustomException 추천
		}
		log.info("authentication :: {}", authentication.getPrincipal());

		return userDetails.getId();
	}
}
