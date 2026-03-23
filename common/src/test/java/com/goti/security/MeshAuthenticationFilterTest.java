package com.goti.security;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

class MeshAuthenticationFilterTest {

	private MeshAuthenticationFilter filter;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private MockFilterChain filterChain;

	@BeforeEach
	void setUp() {
		filter = new MeshAuthenticationFilter();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		filterChain = new MockFilterChain();
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("XFCC 헤더 없으면 인증 설정하지 않고 통과한다")
	void should_skipAuthentication_when_noXfccHeader() throws Exception {
		// Given: XFCC 헤더 없음
		request.addHeader("X-User-Id", UUID.randomUUID().toString());
		request.addHeader("X-User-Role", "MEMBER");

		// When
		filter.doFilterInternal(request, response, filterChain);

		// Then
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	@DisplayName("XFCC + 유효한 X-User-Id/Role이면 SecurityContext에 인증 정보를 설정한다")
	void should_setAuthentication_when_validMeshHeaders() throws Exception {
		// Given
		UUID userId = UUID.randomUUID();
		request.addHeader("X-Forwarded-Client-Cert", "Hash=abc123;Subject=\"\"");
		request.addHeader("X-User-Id", userId.toString());
		request.addHeader("X-User-Role", "ADMIN");

		// When
		filter.doFilterInternal(request, response, filterChain);

		// Then
		var auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNotNull();
		assertThat(auth.isAuthenticated()).isTrue();

		SimpleUserDetails principal = (SimpleUserDetails) auth.getPrincipal();
		assertThat(principal.getId()).isEqualTo(userId);
		assertThat(auth.getAuthorities())
			.extracting("authority")
			.containsExactly("ROLE_ADMIN");
	}

	@Test
	@DisplayName("X-User-Role이 없으면 기본 역할 MEMBER로 설정한다")
	void should_useDefaultRole_when_roleHeaderMissing() throws Exception {
		// Given
		UUID userId = UUID.randomUUID();
		request.addHeader("X-Forwarded-Client-Cert", "Hash=abc123");
		request.addHeader("X-User-Id", userId.toString());
		// X-User-Role 없음

		// When
		filter.doFilterInternal(request, response, filterChain);

		// Then
		var auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNotNull();
		assertThat(auth.getAuthorities())
			.extracting("authority")
			.containsExactly("ROLE_MEMBER");
	}

	@Test
	@DisplayName("허용되지 않은 역할이면 MEMBER로 강제한다")
	void should_fallbackToMember_when_roleNotAllowed() throws Exception {
		// Given
		UUID userId = UUID.randomUUID();
		request.addHeader("X-Forwarded-Client-Cert", "Hash=abc123");
		request.addHeader("X-User-Id", userId.toString());
		request.addHeader("X-User-Role", "SUPER_ADMIN");

		// When
		filter.doFilterInternal(request, response, filterChain);

		// Then
		var auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNotNull();
		assertThat(auth.getAuthorities())
			.extracting("authority")
			.containsExactly("ROLE_MEMBER");
	}

	@Test
	@DisplayName("X-User-Id가 UUID 형식이 아니면 인증 설정하지 않는다")
	void should_skipAuthentication_when_userIdInvalidUuid() throws Exception {
		// Given
		request.addHeader("X-Forwarded-Client-Cert", "Hash=abc123");
		request.addHeader("X-User-Id", "not-a-uuid");
		request.addHeader("X-User-Role", "MEMBER");

		// When
		filter.doFilterInternal(request, response, filterChain);

		// Then
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	@DisplayName("XFCC 있지만 X-User-Id가 없으면 인증 설정하지 않는다")
	void should_skipAuthentication_when_xfccPresentButNoUserId() throws Exception {
		// Given
		request.addHeader("X-Forwarded-Client-Cert", "Hash=abc123");
		// X-User-Id 없음

		// When
		filter.doFilterInternal(request, response, filterChain);

		// Then
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	@DisplayName("소문자 역할도 대문자로 변환하여 처리한다")
	void should_normalizeRoleToUpperCase_when_lowercaseProvided() throws Exception {
		// Given
		UUID userId = UUID.randomUUID();
		request.addHeader("X-Forwarded-Client-Cert", "Hash=abc123");
		request.addHeader("X-User-Id", userId.toString());
		request.addHeader("X-User-Role", "admin");

		// When
		filter.doFilterInternal(request, response, filterChain);

		// Then
		var auth = SecurityContextHolder.getContext().getAuthentication();
		assertThat(auth).isNotNull();
		assertThat(auth.getAuthorities())
			.extracting("authority")
			.containsExactly("ROLE_ADMIN");
	}
}
