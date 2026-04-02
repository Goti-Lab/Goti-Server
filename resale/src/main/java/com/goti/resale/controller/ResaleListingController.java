package com.goti.resale.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.List;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
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
import com.goti.resale.dto.request.ResaleSalesSearchRequest;
import com.goti.resale.dto.response.ResaleListingCountResponse;
import com.goti.resale.dto.response.ResaleListingMyPageCountResponse;
import com.goti.resale.dto.response.ResaleListingOrderCreateResponse;
import com.goti.resale.dto.response.ResaleListingOrderResponse;
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
		summary = "내 판매 내역 조회 (마이페이지)",
		description = "판매 내역 상태별, 기간별 페이징 조회 API"
	)
	@GetMapping("/listings/sales")
	public ResponseEntity<ApiSuccessResponse<Page<ResaleListingResponse>>> getMySales(
		@AuthenticationPrincipal(expression = "id") UUID sellerId,
		@ParameterObject ResaleSalesSearchRequest request
	) {
		Page<ResaleListingResponse> responses = listingService.getMySales(request.toCommand(sellerId));
		return wrap(responses);
	}

	@Operation(
		summary = "리셀 등록 (일괄 포함)",
		description = "티켓 리셀 일괄 등록 및 등급별 주문 그룹 생성 API"
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
		summary = "내 리셀 주문 목록 조회",
		description = "판매자가 등록한 리셀 주문 목록 조회 API"
	)
	@GetMapping("/listings/orders")
	public ResponseEntity<ApiSuccessResponse<List<ResaleListingOrderResponse>>> getListingOrders(
		@AuthenticationPrincipal(expression = "id") UUID sellerId
	) {
		List<ResaleListingOrderResponse> responses = listingService.getListingOrders(sellerId);
		return wrap(responses);
	}

	@Operation(
		summary = "특정 리셀 주문 내 상세 목록 조회",
		description = "특정 리셀 주문에 속한 티켓 상세 목록 조회 API"
	)
	@GetMapping("/listings/orders/{orderId}")
	public ResponseEntity<ApiSuccessResponse<List<ResaleListingResponse>>> getListingsByOrderId(
		@AuthenticationPrincipal(expression = "id") UUID sellerId,
		@PathVariable UUID orderId
	) {
		List<ResaleListingResponse> responses = listingService.getListingsByOrderId(sellerId, orderId);
		return wrap(responses);
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
		summary = "마이페이지 판매 조회",
		description = "마이페이지의 판매중, 판매완료 개수 조회 API"
	)
	@GetMapping("/listings/count/listing")
	public ResponseEntity<ApiSuccessResponse<ResaleListingMyPageCountResponse>> getCountListings(
		@AuthenticationPrincipal(expression = "id") UUID sellerId
	) {
		ResaleListingMyPageCountResponse count = listingService.getCountListings(sellerId);
		return wrap(count);
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
		description = "경기의 전체 리셀 좌석 개수 조회 API"
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
		description = "경기의 등급별 리셀 좌석 개수 조회 API"
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
