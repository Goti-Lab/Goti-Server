package com.goti.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
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
 * <p><b>보안 전제조건</b>: Istio PeerAuthentication STRICT mTLS가 필수.
 * STRICT 모드에서 Istio sidecar는 외부 트래픽의 XFCC 헤더를 sanitize(덮어쓰기)하므로
 * 외부에서 헤더를 위조할 수 없다. PERMISSIVE 모드에서는 비 mTLS 트래픽이
 * sidecar를 우회할 수 있어 XFCC 신뢰가 보장되지 않는다.</p>
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
	protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
		String clientCert = request.getHeader(HEADER_CLIENT_CERT);
		return clientCert == null || clientCert.isBlank();
	}

	@Override
	protected void doFilterInternal(
		@NonNull HttpServletRequest request,
		@NonNull HttpServletResponse response,
		@NonNull FilterChain filterChain
	) throws ServletException, IOException {

		String userId = request.getHeader(HEADER_USER_ID);
		if (userId == null || userId.isBlank()) {
			filterChain.doFilter(request, response);
			return;
		}

		UUID id = parseUserId(userId, request.getRequestURI());
		if (id == null) {
			filterChain.doFilter(request, response);
			return;
		}

		String role = resolveRole(request.getHeader(HEADER_USER_ROLE));
		setAuthentication(id, role);
		log.debug("Mesh 인증 설정: userId={}, role={}", id, role);

		filterChain.doFilter(request, response);
	}

	private UUID parseUserId(String userId, String requestUri) {
		try {
			return UUID.fromString(userId);
		} catch (IllegalArgumentException e) {
			log.warn("X-User-Id UUID 파싱 실패: userId={}, path={}", userId, requestUri);
			return null;
		}
	}

	private String resolveRole(String roleHeader) {
		if (roleHeader != null && ALLOWED_ROLES.contains(roleHeader.toUpperCase())) {
			return roleHeader.toUpperCase();
		}
		return DEFAULT_ROLE;
	}

	private void setAuthentication(UUID id, String role) {
		SimpleUserDetails userDetails = new SimpleUserDetails(id, role);
		UsernamePasswordAuthenticationToken authentication =
			UsernamePasswordAuthenticationToken.authenticated(
				userDetails, null, userDetails.getAuthorities()
			);

		SecurityContext context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
	}
}
