package com.goti.resale.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.resale.dto.response.ResaleListingMyPageCountResponse;
import com.goti.resale.service.application.ResaleListingProcessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Internal Resale", description = "내부 리셀 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/internal/resales")
public class ResaleInternalController {
	private final ResaleListingProcessService listingProcessService;

	@Operation(
		summary = "마이페이지 판매 조회 (내부용)",
		description = "특정 사용자의 판매중, 판매완료 갯수 조회 API"
	)
	@GetMapping("/listings/count/listing")
	public ResponseEntity<ApiSuccessResponse<ResaleListingMyPageCountResponse>> getCountListings(
		@RequestParam UUID sellerId
	) {
		ResaleListingMyPageCountResponse count = listingProcessService.getCountListings(sellerId);
		return wrap(count);
	}
}
