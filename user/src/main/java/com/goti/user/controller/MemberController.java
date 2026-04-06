package com.goti.user.controller;

import com.goti.constants.OAuthProvider;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.dto.request.AccountRegisterRequest;
import com.goti.user.dto.request.AddressRegisterRequest;
import com.goti.user.dto.request.MemberUpdateRequest;
import com.goti.user.dto.response.AccountRegisterResponse;
import com.goti.user.dto.response.AddressRegisterResponse;
import com.goti.user.dto.response.MemberDetailResponse;
import com.goti.user.dto.response.MemberUpdateResponse;
import com.goti.user.service.application.member.MemberProfileService;

import com.goti.user.service.domain.user.MemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import static com.goti.global.api.ApiSuccessResponse.wrap;

@Slf4j
@Tag(name = "Member", description = "회원 관련 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberProfileService memberProfileService;
	private final MemberService memberService;

	@Operation(
		summary = "회원 상세 조회(본인)",
		description = "회원 상세 조회(본인) API"
	)
	@GetMapping("/me")
	public ResponseEntity<ApiSuccessResponse<MemberDetailResponse>> detail(
		@AuthenticationPrincipal(expression = "providerId") String providerId,
		@AuthenticationPrincipal(expression = "provider") OAuthProvider provider
	) {
		return wrap(
			memberProfileService.getProfileDetail(providerId, provider)
		);
	}

	@Operation(
		summary = "계좌 등록",
		description = "회원 계좌 등록 API"
	)
	@PostMapping("/accounts")
	public ResponseEntity<ApiSuccessResponse<AccountRegisterResponse>> registerAccount(
		@AuthenticationPrincipal(expression = "id") UUID memberId,
		@RequestBody @Valid AccountRegisterRequest request
	) {
		return wrap(
			memberProfileService.registerAccount(
				request.accountNumber(),
				request.bankName(),
				request.accountHolder(),
				memberId
			)
		);
	}

	@Operation(
		summary = "주소 등록",
		description = "회원 주소 등록 API"
	)
	@PostMapping("/addresses")
	public ResponseEntity<ApiSuccessResponse<AddressRegisterResponse>> registerAddress(
		@AuthenticationPrincipal(expression = "id") UUID memberId,
		@RequestBody @Valid AddressRegisterRequest request
	) {
		return wrap(
			memberProfileService.registerAddress(
				memberId,
				request.zipCode(),
				request.baseAddress(),
				request.detailAddress()
			)
		);
	}

	@Operation(
		summary = "회원 정보 수정(본인)",
		description = "회원 정보(이름 및 휴대전화번호) 수정(본인) API"
	)
	@PatchMapping("/me")
	public ResponseEntity<ApiSuccessResponse<MemberUpdateResponse>> update(
		@AuthenticationPrincipal(expression = "id") UUID memberId,
		@RequestBody @Valid MemberUpdateRequest request
	) {
		return wrap(
			memberService.update(
				memberId,
				request.mobile(),
				request.name(),
				request.gender(),
				request.birthDate(),
				request.authCode()
			)
		);
	}

}
