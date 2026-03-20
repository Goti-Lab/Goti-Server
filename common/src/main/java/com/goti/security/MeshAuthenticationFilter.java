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
import java.util.Set;
import java.util.UUID;

/**
 * Istio sidecar가 JWT를 검증하고 주입한 X-User-Id / X-User-Role 헤더를
 * Spring SecurityContext로 변환하는 필터.
 *
 * <p>MSA 분리 서비스(ticketing, payment, resale, stadium)에서 사용한다.
 * user 서비스는 기존 JwtAuthenticationFilter를 유지한다.</p>
 *
 * <p>보안: X-Forwarded-Client-Cert 헤더로 mesh 환경을 확인한다.
 * Istio 미적용 환경에서는 헤더를 신뢰하지 않아 인증 우회를 방지한다.</p>
 *
 * <p>외부 의존성 없으므로 Bean 등록 불필요 — new로 직접 생성한다.</p>
 */
@Slf4j
public class MeshAuthenticationFilter extends OncePerRequestFilter {

	private static final String HEADER_USER_ID = "X-User-Id";
	private static final String HEADER_USER_ROLE = "X-User-Role";
	private static final String HEADER_CLIENT_CERT = "X-Forwarded-Client-Cert";
	private static final String DEFAULT_ROLE = "MEMBER";
	private static final Set<String> ALLOWED_ROLES = Set.of("MEMBER", "ADMIN");

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {

		// mesh 환경 확인: Istio mTLS가 주입하는 XFCC 헤더 존재 여부
		String clientCert = request.getHeader(HEADER_CLIENT_CERT);
		if (clientCert == null || clientCert.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}

		String userId = request.getHeader(HEADER_USER_ID);

		if (userId != null && !userId.isBlank()) {
			try {
				UUID id = UUID.fromString(userId);
				String role = request.getHeader(HEADER_USER_ROLE);
				if (role == null || !ALLOWED_ROLES.contains(role.toUpperCase())) {
					role = DEFAULT_ROLE;
				}

				SimpleUserDetails userDetails = new SimpleUserDetails(id, role.toUpperCase());
				UsernamePasswordAuthenticationToken authentication =
					UsernamePasswordAuthenticationToken.authenticated(
						userDetails,
						null,
						userDetails.getAuthorities()
					);
				SecurityContextHolder.getContext().setAuthentication(authentication);

				log.debug("Mesh 인증 설정: userId={}, role={}", id, role);
			} catch (IllegalArgumentException e) {
				log.warn("X-User-Id UUID 파싱 실패: userId={}, path={}", userId, request.getRequestURI());
			}
		}

		filterChain.doFilter(request, response);
	}
}
