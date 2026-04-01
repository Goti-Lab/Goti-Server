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
import com.goti.resale.dto.request.ResaleListingOrderCreateRequest;
import com.goti.resale.dto.response.ResaleListingCountResponse;
import com.goti.resale.dto.response.ResaleListingOrderCreateResponse;
import com.goti.resale.dto.response.ResaleListingResponse;
import com.goti.resale.dto.response.ResalePriceHistoryResponse;
import com.goti.resale.service.application.ResaleListingProcessService;
import com.goti.resale.service.application.ResalePriceProcessService;

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

	private final ResaleListingProcessService listingService;
	private final ResalePriceProcessService priceService;

	@Operation(
		summary = "리셀 등록 (일괄 포함)",
		description = "티켓을 한 번에 리셀 등록하며, 등급별로 주문 그룹을 생성하여 등록 API"
	)
	@PostMapping("/listings")
	public ResponseEntity<ApiSuccessResponse<ResaleListingOrderCreateResponse>> createListingOrder(
		@AuthenticationPrincipal(expression = "id") UUID sellerId,
		@Valid @RequestBody ResaleListingOrderCreateRequest request
	) {

		ResaleListingOrderCreateResponse response = listingService.createListingOrder(sellerId, request);
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
		summary = "등록 일괄 취소",
		description = "리셀 등록 그룹에 속한 모든 판매 중인 티켓을 취소 API"
	)
	@PatchMapping("/listings/orders/{listingOrderId}/cancel")
	public ResponseEntity<ApiSuccessResponse<Void>> cancelListingOrder(
		@AuthenticationPrincipal(expression = "id") UUID sellerId,
		@PathVariable UUID listingOrderId
	) {
		listingService.cancelListingOrder(sellerId, listingOrderId);
		return empty();
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
		summary = "경기의 등급별 리셀 좌석 개수 조회",
		description = "경기의 등급별 리셀 좌석의 갯수 조회 API"
	)
	@GetMapping("/listings/games/{gameId}/grade/{gradeId}/count")
	public ResponseEntity<ApiSuccessResponse<ResaleListingCountResponse>> getListingCountByGrade(
		@PathVariable UUID gameId,
		@PathVariable UUID gradeId
	) {
		long count = listingService.getListingCountByGrade(gameId, gradeId);
		return wrap(new ResaleListingCountResponse(count));
	}
}
