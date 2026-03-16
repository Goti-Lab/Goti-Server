package com.goti.ticketing.pricing.dto.request;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.goti.ticketing.constants.TicketPricingDayType;
import com.goti.ticketing.constants.TicketPricingMatchType;
import com.goti.ticketing.constants.TicketType;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record TicketPricingPolicyCreateRequest(
	@Schema(description = "정책 시작일", example = "2026-03-01")
	@NotNull(message = "정책 시작일은 필수입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate policyStartAt,

	@Schema(description = "정책 종료일", example = "2026-10-31")
	@NotNull(message = "정책 종료일은 필수입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate policyEndAt,

	@Schema(description = "가격 정책 상세 목록")
	@NotEmpty(message = "가격 정책 상세 목록은 비어 있을 수 없습니다.")
	List<@Valid TicketPriceCreateRequest> prices
) {
	public record TicketPriceCreateRequest(
		@Schema(description = "좌석 등급 ID", example = "22222222-2222-2222-2222-222222222222")
		@NotNull(message = "좌석 등급 ID는 필수입니다.")
		UUID gradeId,

		@Schema(description = "권종", example = "ADULT")
		@NotNull(message = "권종은 필수입니다.")
		TicketType ticketType,

		@Schema(description = "요일 유형", example = "WEEKDAY")
		@NotNull(message = "요일 유형은 필수입니다.")
		TicketPricingDayType dayType,

		@Schema(description = "매치 유형", example = "REGULAR")
		@NotNull(message = "매치 유형은 필수입니다.")
		TicketPricingMatchType matchType,

		@Schema(description = "가격", example = "15000")
		@NotNull(message = "가격은 필수입니다.")
		Integer price
	) {
	}
}
