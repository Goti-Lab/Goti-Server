package com.goti.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Istio sidecar가 JWT를 검증하고 주입한 X-User-Id / X-User-Role 헤더를
 * Spring SecurityContext로 변환하는 필터.
 *
 * <p>MSA 분리 서비스(ticketing, payment, resale, stadium)에서 사용한다.
 * user 서비스는 기존 JwtAuthenticationFilter를 유지한다.</p>
 *
 * <p>동작 순서:</p>
 * <ol>
 *   <li>X-User-Id 헤더 존재 확인</li>
 *   <li>SimpleUserDetails 생성 (DB 조회 없음)</li>
 *   <li>SecurityContext에 Authentication 설정</li>
 *   <li>헤더 없으면 → 인증 미설정 → authenticated() 체크에서 401</li>
 * </ol>
 */
@Slf4j
public class MeshAuthenticationFilter extends OncePerRequestFilter {

	private static final String HEADER_USER_ID = "X-User-Id";
	private static final String HEADER_USER_ROLE = "X-User-Role";
	private static final String DEFAULT_ROLE = "MEMBER";

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {

		String userId = request.getHeader(HEADER_USER_ID);

		if (userId != null && !userId.isBlank()) {
			try {
				UUID id = UUID.fromString(userId);
				String role = request.getHeader(HEADER_USER_ROLE);
				if (role == null || role.isBlank()) {
					role = DEFAULT_ROLE;
				}

				SimpleUserDetails userDetails = new SimpleUserDetails(id, role);
				UsernamePasswordAuthenticationToken authentication =
					UsernamePasswordAuthenticationToken.authenticated(
						userDetails,
						null,
						userDetails.getAuthorities()
					);
				SecurityContextHolder.getContext().setAuthentication(authentication);

				log.debug("Mesh 인증 설정: userId={}, role={}", id, role);
			} catch (IllegalArgumentException e) {
				log.warn("X-User-Id UUID 파싱 실패: {}", userId);
			}
		}

		filterChain.doFilter(request, response);
	}
}
