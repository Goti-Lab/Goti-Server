package com.goti.controller;

import com.goti.constants.OAuthProvider;
import com.goti.dto.request.LoginRequest;
import com.goti.dto.request.SocialVerifyRequest;
import com.goti.dto.response.SocialVerifyResponse;
import com.goti.dto.response.TokenResponse;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.infra.api.dto.response.common.SocialStateResponse;

import com.goti.service.auth.application.SocialAuthService;

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

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final SocialAuthService socialAuthService;

	@GetMapping("/{provider}/state")
	public ResponseEntity<ApiSuccessResponse<SocialStateResponse>> issueState(
		@PathVariable OAuthProvider provider
	) {
		return wrap(
			socialAuthService.issueState(provider)
		);
	}

	@PostMapping("/{provider}/social/verify")
	public ResponseEntity<ApiSuccessResponse<SocialVerifyResponse>> verify(
		@PathVariable OAuthProvider provider,
		@RequestBody @Valid SocialVerifyRequest request
	) {
		return wrap(
			socialAuthService.verify(provider, request.authCode(), request.state())
		);
	}

	@PostMapping("/login")
	public ResponseEntity<ApiSuccessResponse<TokenResponse>> login(
		@RequestBody @Valid LoginRequest request
	) {
		String accessToken = socialAuthService.login(
			request.socialVerifyToken()
		).getFirst();
		return wrap(new TokenResponse(accessToken));
	}
}
