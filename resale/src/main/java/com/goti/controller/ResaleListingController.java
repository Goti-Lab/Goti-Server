package com.goti.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.dto.request.ResaleListingCreateRequest;
import com.goti.dto.response.ResaleListingResponse;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.service.ResaleListingService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/resale/listings")
@RequiredArgsConstructor
public class ResaleListingController {

	private final ResaleListingService listingService;

	@PostMapping
	public ResponseEntity<ApiSuccessResponse<ResaleListingResponse>> createListing(
		@RequestParam(required = false) UUID sellerId, // TODO : 로그인 구현완료시 로그인으로 받아올 것
		@Valid @RequestBody ResaleListingCreateRequest request
	) {
		ResaleListingResponse response = listingService.createListing(sellerId, request);
		return ApiSuccessResponse.wrap(response);
	}
}
