package com.goti.resale.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.resale.dto.response.ResaleListingsCountResponse;
import com.goti.resale.service.application.ResaleListingProcessService;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/resales")
public class InternalResaleController {
	private final ResaleListingProcessService listingService;

	@GetMapping("/listings/count")
	public ResponseEntity<ApiSuccessResponse<ResaleListingsCountResponse>> getResaleCount(
		@RequestParam UUID userId
	) {
		return wrap(listingService.getResaleCount(userId));
	}
}
