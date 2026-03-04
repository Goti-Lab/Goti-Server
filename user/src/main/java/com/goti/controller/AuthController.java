package com.goti.controller;

import com.goti.constants.OAuthProvider;
import com.goti.dto.request.LoginRequest;
import com.goti.dto.response.LoginResponse;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.service.auth.application.AuthApplicationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
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

	private final AuthApplicationService authApplicationService;

	@PostMapping("/{provider}/login")
	public ResponseEntity<ApiSuccessResponse<LoginResponse>> login(
		@PathVariable OAuthProvider provider,
		@RequestBody @Valid LoginRequest request
	) {
		return wrap(
			authApplicationService.login(
				provider, request.authCode(), request.state()
			)
		);
	}
}
