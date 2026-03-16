package com.goti.ticketing.pricing.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.global.api.ApiSuccessResponse;
import com.goti.ticketing.pricing.dto.request.TicketPricingPolicyCreateRequest;
import com.goti.ticketing.pricing.dto.response.TicketPricingPolicyCreateResponse;
import com.goti.ticketing.pricing.service.domain.TicketPricingPolicyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Ticket Pricing Policy", description = "가격 정책 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams/{teamId}/ticket-pricing-policies")
public class TicketPricingPolicyController {
	private final TicketPricingPolicyService ticketPricingPolicyService;

	@Operation(
		summary = "가격 정책 생성",
		description = "관리자가 가격 정책과 티켓 가격 상세를 함께 생성하는 API"
	)
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<TicketPricingPolicyCreateResponse>> create(
		@PathVariable UUID teamId,
		@Valid @RequestBody TicketPricingPolicyCreateRequest request
	) {
		// TODO: 추후 관리자 인증 도입 후 관리자만 생성 가능하도록 처리
		TicketPricingPolicyCreateResponse response = ticketPricingPolicyService.create(
			teamId,
			request.policyStartAt(),
			request.policyEndAt(),
			request.prices().stream()
				.map(price -> new TicketPricingPolicyService.TicketPriceCreateParam(
					price.gradeId(),
					price.ticketType(),
					price.dayType(),
					price.matchType(),
					price.price()
				))
				.toList()
		);
		return wrap(response);
	}
}
