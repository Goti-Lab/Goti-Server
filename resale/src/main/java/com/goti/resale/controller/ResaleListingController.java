package com.goti.resale.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.resale.constants.ResaleGraphRange;
import com.goti.resale.dto.request.ResaleListingCancelRequest;
import com.goti.resale.dto.request.ResaleListingCreateRequest;
import com.goti.resale.dto.response.ResaleListingCountResponse;
import com.goti.resale.dto.response.ResaleListingResponse;
import com.goti.resale.dto.response.ResalePriceHistoryResponse;
import com.goti.resale.service.application.ResaleListingService;
import com.goti.resale.service.application.ResalePriceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Resale Listing", description = "리셀 등록 API")
@RestController
@RequestMapping("/api/v1/resales")
@RequiredArgsConstructor
public class ResaleListingController {

	private final ResaleListingService listingService;
	private final ResalePriceService priceService;

	@Operation(
		summary = "리셀 등록",
		description = "리셀 등록 API"
	)
	@PostMapping("/listings")
	public ResponseEntity<ApiSuccessResponse<ResaleListingResponse>> createListing(
		@AuthenticationPrincipal(expression = "id") UUID sellerId,
		@Valid @RequestBody ResaleListingCreateRequest request
	) {

		ResaleListingResponse response = listingService.createListing(sellerId, request);
		return wrap(response);
	}

	@Operation(
		summary = "등록 취소",
		description = "리셀 취소 API"
	)
	@PatchMapping("/listings/cancel")
	public ResponseEntity<ApiSuccessResponse<ResaleListingResponse>> cancelListing(
		@AuthenticationPrincipal(expression = "id") UUID sellerId,
		@Valid @RequestBody ResaleListingCancelRequest request
	) {

		ResaleListingResponse response = listingService.cancelListing(sellerId, request);
		return wrap(response);
	}

	@Operation(
		summary = "목록 조회",
		description = "판매자의 리셀 목록 조회 API"
	)
	@GetMapping("/listings")
	public ResponseEntity<ApiSuccessResponse<List<ResaleListingResponse>>> getListingsBySellerId(
		@AuthenticationPrincipal(expression = "id") UUID sellerId
	) {
		List<ResaleListingResponse> responses = listingService.getListingsBySellerId(sellerId);
		return wrap(responses);
	}

	@Operation(
		summary = "경기의 판매 기록 조회",
		description = "경기의 지난 시간(HOUR, DAY, WEEK) 내 등급 별 판매 기록 조회 API"
	)
	@GetMapping("/histories/games/{gameId}/grade/{gradeId}/ranges/{range}/graph")
	public ResponseEntity<ApiSuccessResponse<List<ResalePriceHistoryResponse>>> getListPriceHistory(
		@PathVariable UUID gameId,
		@PathVariable UUID gradeId,
		@PathVariable ResaleGraphRange range
	) {
		List<ResalePriceHistoryResponse> responses = priceService.getHistory(gameId, gradeId, range);
		return wrap(responses);
	}

	@Operation(
		summary = "경기의 전체 리셀 좌석 개수 조회",
		description = "경기의 전체 리셀 좌석의 갯수 조회 API"
	)
	@GetMapping("/listings/games/{gameId}/count")
	public ResponseEntity<ApiSuccessResponse<ResaleListingCountResponse>> getTotalListingCount(
		@PathVariable UUID gameId
	) {
		long count = listingService.getTotalListingCount(gameId);
		return wrap(new ResaleListingCountResponse(count));
	}

	@Operation(
		summary = "경기의 구역별 리셀 좌석 개수 조회",
		description = "경기의 구역별 리셀 좌석의 갯수 조회 API"
	)
	@GetMapping("/listings/games/{gameId}/section/{sectionId}/count")
	public ResponseEntity<ApiSuccessResponse<ResaleListingCountResponse>> getListingCountBySection(
		@PathVariable UUID gameId,
		@PathVariable UUID sectionId
	) {
		long count = listingService.getListingCountBySection(gameId, sectionId);
		return wrap(new ResaleListingCountResponse(count));
	}
}
