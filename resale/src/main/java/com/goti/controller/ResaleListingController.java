package com.goti.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.dto.request.ResaleListingCancelRequest;
import com.goti.dto.request.ResaleListingCreateRequest;
import com.goti.dto.response.ResaleListingResponse;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.service.ResaleListingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "리셀 등록", description = "리셀 등록 API")
@RestController
@RequestMapping("/api/v1/resale/listings")
@RequiredArgsConstructor
public class ResaleListingController {

	private final ResaleListingService listingService;

	@Operation(
		summary = "리셀 등록",
		description = "리셀 등록 API"
	)
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<ResaleListingResponse>> createListing(
		@RequestParam(required = false) UUID sellerId, // TODO : 로그인 구현완료시 로그인으로 받아올 것
		@Valid @RequestBody ResaleListingCreateRequest request
	) {
		ResaleListingResponse response = listingService.createListing(sellerId, request);
		return ApiSuccessResponse.wrap(response);
	}

	@Operation(
		summary = "등록 취소",
		description = "리셀 취소 API"
	)
	@PutMapping("/cancel")
	public ResponseEntity<ApiSuccessResponse<ResaleListingResponse>> cancelListing(
		@RequestParam(required = false) UUID sellerId, // TODO : 로그인 구현완료시 로그인으로 받아올 것
		@Valid @RequestBody ResaleListingCancelRequest request
	) {
		ResaleListingResponse response = listingService.cancelListing(sellerId, request);
		return ApiSuccessResponse.wrap(response);
	}

	@Operation(
		summary = "목록 조회",
		description = "판매자의 리셀 목록 조회 API"
	)
	@GetMapping
	public ResponseEntity<ApiSuccessResponse<List<ResaleListingResponse>>> getListingsBySellerId(
		@RequestParam(required = false) UUID sellerId // TODO : 로그인 구현완료시 로그인으로 받아올 것
	) {
		List<ResaleListingResponse> responses = listingService.getListingsBySellerId(sellerId);
		return ApiSuccessResponse.wrap(responses);
	}

}
