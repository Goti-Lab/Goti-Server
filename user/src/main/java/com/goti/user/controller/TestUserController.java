package com.goti.user.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.user.dto.request.BulkCreateTestUserRequest;
import com.goti.user.dto.request.CreateTestUserRequest;
import com.goti.user.dto.request.TestLoginRequest;
import com.goti.user.dto.response.BulkTestUserResponse;
import com.goti.user.dto.response.TestUserResponse;
import com.goti.user.dto.response.TokenResponse;
import com.goti.user.service.test.TestUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@ConditionalOnProperty(name = "goti.test-user.enabled", havingValue = "true", matchIfMissing = true)
@Tag(name = "Test User", description = "테스트 유저 생성/로그인 API (goti.test-user.enabled=true)")
@RestController
@RequestMapping("/api/v1/test/users")
@RequiredArgsConstructor
public class TestUserController {

	private final TestUserService testUserService;

	@Operation(
		summary = "테스트 유저 단건 생성",
		description = "OAuth/SMS 인증 없이 테스트 유저를 생성하고 JWT 토큰을 발급합니다. "
			+ "동일 mobile이 이미 존재하면 기존 유저의 새 토큰을 반환합니다."
	)
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<TestUserResponse>> createUser(
		@RequestBody @Valid CreateTestUserRequest request
	) {
		return wrap(testUserService.createUser(request));
	}

	@Operation(
		summary = "테스트 유저 대량 생성",
		description = "K6 부하 테스트용 유저를 대량 생성합니다. "
			+ "mobile: 000{startIndex:08d} ~ 000{startIndex+count-1:08d}, "
			+ "이미 존재하는 유저는 skip합니다."
	)
	@PostMapping("/bulk")
	public ResponseEntity<ApiSuccessResponse<BulkTestUserResponse>> bulkCreateUsers(
		@RequestBody @Valid BulkCreateTestUserRequest request
	) {
		return wrap(testUserService.bulkCreateUsers(request));
	}

	@Operation(
		summary = "테스트 유저 로그인",
		description = "mobile 번호로 테스트 유저의 JWT 토큰을 발급합니다. "
			+ "K6 시나리오에서 bulk 생성 후 개별 토큰 발급에 사용합니다."
	)
	@PostMapping("/login")
	public ResponseEntity<ApiSuccessResponse<TokenResponse>> login(
		@RequestBody @Valid TestLoginRequest request
	) {
		return wrap(testUserService.login(request));
	}
}
