package com.goti.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.dto.request.ResaleListingCancelRequest;
import com.goti.dto.request.ResaleListingCreateRequest;
import com.goti.dto.response.ResaleListingResponse;
import com.goti.global.annotation.LoginUserId;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.service.application.ResaleListingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Resale Listing", description = "리셀 등록 API")
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
		@LoginUserId UUID sellerId,
		@Valid @RequestBody ResaleListingCreateRequest request
	) {
		ResaleListingResponse response = listingService.createListing(sellerId, request);
		return wrap(response);
	}

	@Operation(
		summary = "등록 취소",
		description = "리셀 취소 API"
	)
	@PutMapping("/cancel")
	public ResponseEntity<ApiSuccessResponse<ResaleListingResponse>> cancelListing(
		@LoginUserId UUID sellerId,
		@Valid @RequestBody ResaleListingCancelRequest request
	) {
		ResaleListingResponse response = listingService.cancelListing(sellerId, request);
		return wrap(response);
	}

	@Operation(
		summary = "목록 조회",
		description = "판매자의 리셀 목록 조회 API"
	)
	@GetMapping
	public ResponseEntity<ApiSuccessResponse<List<ResaleListingResponse>>> getListingsBySellerId(
		@LoginUserId UUID sellerId
	) {
		List<ResaleListingResponse> responses = listingService.getListingsBySellerId(sellerId);
		return wrap(responses);
	}

}
