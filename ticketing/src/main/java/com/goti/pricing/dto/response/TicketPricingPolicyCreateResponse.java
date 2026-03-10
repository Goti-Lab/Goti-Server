package com.goti.pricing.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.goti.constants.TicketPricingDayType;
import com.goti.constants.TicketPricingMatchType;
import com.goti.constants.TicketType;
import com.goti.domain.entity.pricing.TicketPriceEntity;
import com.goti.domain.entity.pricing.TicketPricingPolicyEntity;

import io.swagger.v3.oas.annotations.media.Schema;

public record TicketPricingPolicyCreateResponse(
	@Schema(description = "가격 정책 ID", example = "22222222-2222-2222-2222-222222222222")
	UUID policyId,

	@Schema(description = "팀 ID", example = "11111111-1111-1111-1111-111111111111")
	UUID teamId,

	@Schema(description = "정책 시작일", example = "2026-03-01")
	LocalDate policyStartAt,

	@Schema(description = "정책 종료일", example = "2026-10-31")
	LocalDate policyEndAt,

	@Schema(description = "활성 여부", example = "true")
	boolean isActive,

	@Schema(description = "가격 정책 상세 목록")
	List<TicketPriceResponse> prices
) {
	public static TicketPricingPolicyCreateResponse from(
		TicketPricingPolicyEntity policy,
		List<TicketPriceEntity> prices
	) {
		return new TicketPricingPolicyCreateResponse(
			policy.getId(),
			policy.getTeamId(),
			policy.getPolicyStartAt(),
			policy.getPolicyEndAt(),
			policy.isActive(),
			prices.stream()
				.map(TicketPriceResponse::from)
				.toList()
		);
	}

	public record TicketPriceResponse(
		@Schema(description = "티켓 가격 ID", example = "33333333-3333-3333-3333-333333333333")
		UUID priceId,

		@Schema(description = "좌석 등급 ID", example = "22222222-2222-2222-2222-222222222222")
		UUID gradeId,

		@Schema(description = "권종", example = "ADULT")
		TicketType ticketType,

		@Schema(description = "요일 유형", example = "WEEKDAY")
		TicketPricingDayType dayType,

		@Schema(description = "매치 유형", example = "REGULAR")
		TicketPricingMatchType matchType,

		@Schema(description = "가격", example = "15000")
		Integer price
	) {
		public static TicketPriceResponse from(TicketPriceEntity ticketPrice) {
			return new TicketPriceResponse(
				ticketPrice.getId(),
				ticketPrice.getGrade().getId(),
				ticketPrice.getTicketType(),
				ticketPrice.getDayType(),
				ticketPrice.getMatchType(),
				ticketPrice.getPrice()
			);
		}
	}
}
