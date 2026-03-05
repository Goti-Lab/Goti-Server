package com.goti.controller;

import com.goti.constants.OAuthProvider;
import com.goti.dto.request.LoginRequest;
import com.goti.dto.request.SignupRequest;
import com.goti.dto.request.SocialVerifyRequest;
import com.goti.dto.response.SocialVerifyResponse;
import com.goti.dto.response.TokenResponse;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.infra.api.dto.response.common.SocialStateResponse;

import com.goti.service.auth.application.SocialAuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.goti.global.api.ApiSuccessResponse.wrap;

@Tag(name = "Auth", description = "소셜 로그인 및 회원가입 인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final SocialAuthService socialAuthService;

	@Operation(
		summary = "소셜 인증 State 발급",
		description = "소셜 로그인(Naver, Google) 요청 전 CSRF 방지 State 발급 API"
	)
	@GetMapping("/{provider}/state")
	public ResponseEntity<ApiSuccessResponse<SocialStateResponse>> issueState(
		@PathVariable OAuthProvider provider
	) {
		return wrap(
			socialAuthService.issueState(provider)
		);
	}

	@Operation(
		summary = "소셜 코드 검증 및 임시토큰 발급",
		description = "AuthCode를 검증 및 socialVerifyToken 발급 API"
	)
	@PostMapping("/{provider}/social/verify")
	public ResponseEntity<ApiSuccessResponse<SocialVerifyResponse>> verify(
		@PathVariable OAuthProvider provider,
		@RequestBody @Valid SocialVerifyRequest request
	) {
		return wrap(
			socialAuthService.verify(provider, request.authCode(), request.state())
		);
	}

	@Operation(
		summary = "기존 회원 로그인",
		description = "socialVerifyToken 기반 로그인 API"
	)
	@PostMapping("/login")
	public ResponseEntity<ApiSuccessResponse<TokenResponse>> login(
		@RequestBody @Valid LoginRequest request
	) {
		String accessToken = socialAuthService.login(
			request.socialVerifyToken()
		).getFirst();
		return wrap(new TokenResponse(accessToken));
	}

	@Operation(
		summary = "신규 회원 가입",
		description = "socialVerifyToken 및 추가 사용자 정보 기반 회원가입 API"
	)
	@PostMapping("/signup")
	public ResponseEntity<ApiSuccessResponse<TokenResponse>> signup(
		@RequestBody @Valid SignupRequest request
	) {
		String accessToken = socialAuthService.signup(
			request.socialVerifyToken(),
			request.email(),
			request.name(),
			request.mobile(),
			request.gender(),
			request.birthDate()
		).getFirst();

		return wrap(new TokenResponse(accessToken));
	}
}
