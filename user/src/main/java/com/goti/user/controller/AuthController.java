package com.goti.user.controller;

import com.goti.constants.OAuthProvider;
import com.goti.user.dto.request.LoginRequest;
import com.goti.user.dto.request.SendSmsRequest;
import com.goti.user.dto.request.SignupRequest;
import com.goti.user.dto.request.SocialVerifyRequest;
import com.goti.user.dto.response.SocialVerifyResponse;
import com.goti.user.dto.response.TokenResponse;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.infra.api.dto.response.common.SocialStateResponse;

import com.goti.user.service.auth.application.SocialAuthService;

import com.goti.user.util.CookieProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.goti.global.api.ApiSuccessResponse.*;

@Tag(name = "Auth", description = "소셜 로그인 및 회원가입 인증 관련 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final SocialAuthService socialAuthService;
	private final CookieProvider cookieProvider;

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
		@RequestBody @Valid LoginRequest request, HttpServletResponse response
	) {

		Pair<String, String> tokens = socialAuthService.login(
			request.socialVerifyToken()
		);

		String accessToken = tokens.getFirst();
		String refreshToken = tokens.getSecond();

		ResponseCookie cookie = cookieProvider.createRefreshTokenCookie(refreshToken);
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

		return wrap(new TokenResponse(accessToken));
	}

	@Operation(
		summary = "회원 가입 및 사용자 식별",
		description = "사용자 정보 등록(가입) 또는 기존 계정 식별 및 인증번호 검증 API"
	)
	@PostMapping("/signup")
	public ResponseEntity<ApiSuccessResponse<TokenResponse>> signup(
		@RequestBody @Valid SignupRequest request, HttpServletResponse response
	) {
		Pair<String, String> tokens = socialAuthService.signup(
			request.socialVerifyToken(),
			request.name(),
			request.mobile(),
			request.gender(),
			request.birthDate(),
			request.authCode()
		);
		String accessToken = tokens.getFirst();
		String refreshToken = tokens.getSecond();
		ResponseCookie cookie = cookieProvider.createRefreshTokenCookie(refreshToken);
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		return wrap(new TokenResponse(accessToken));
	}

	@Operation(
		summary = "회원가입 인증번호 발송",
		description = "socialVerifyToken 및 휴대폰 번호 기반 인증번호 발송 API"
	)
	@PostMapping("/signup/sms/send")
	public ResponseEntity<ApiSuccessResponse<Void>> sendSmsCode(
		@RequestBody @Valid SendSmsRequest request
	) {
		socialAuthService.sendSignupSmsCode(
			request.socialVerifyToken(), request.mobile()
		);
		return empty();
	}
}
