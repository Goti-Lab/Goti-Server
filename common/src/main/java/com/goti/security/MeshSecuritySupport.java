package com.goti.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * MSA 서비스 SecurityConfig 공통 설정.
 * CSRF disable, STATELESS 세션, MeshAuthenticationFilter 등록,
 * 401/403 JSON 응답 핸들링을 한 곳에서 관리한다.
 *
 * <p>{@code meshEnabled=true}: Istio mesh 환경. MeshAuthenticationFilter 등록 + authenticated().
 * {@code meshEnabled=false}: 로컬/테스트 환경. 필터 미등록 + permitAll().</p>
 */
public final class MeshSecuritySupport {

	private static final String UNAUTHORIZED_BODY =
		"{\"code\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}";
	private static final String FORBIDDEN_BODY =
		"{\"code\":\"FORBIDDEN\",\"message\":\"접근 권한이 없습니다.\"}";

	/** MSA 서비스 공통 공개 경로 (health + swagger + API docs) */
	public static final String[] PUBLIC_PATHS = {
		"/actuator/health",
		"/actuator/health/**",
		"/swagger-ui/**",
		"/v3/api-docs/**"
	};

	private MeshSecuritySupport() {
	}

	/**
	 * MSA 서비스 공통 보안 설정을 적용한다.
	 *
	 * @param http        HttpSecurity
	 * @param meshEnabled true면 mesh 인증 활성화, false면 전체 permitAll (로컬 개발용)
	 */
	public static HttpSecurity applyDefaults(HttpSecurity http, boolean meshEnabled) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			);

		if (meshEnabled) {
			http
				.exceptionHandling(exceptions -> exceptions
					.authenticationEntryPoint((request, response, authException) ->
						writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED_BODY))
					.accessDeniedHandler((request, response, accessDeniedException) ->
						writeJsonError(response, HttpServletResponse.SC_FORBIDDEN, FORBIDDEN_BODY))
				)
				.addFilterBefore(
					new MeshAuthenticationFilter(),
					UsernamePasswordAuthenticationFilter.class
				);
		}

		return http;
	}

	private static void writeJsonError(HttpServletResponse response, int status, String body)
		throws java.io.IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(body);
	}
}
