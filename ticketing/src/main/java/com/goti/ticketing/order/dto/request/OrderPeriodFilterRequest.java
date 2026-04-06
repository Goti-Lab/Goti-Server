package com.goti.ticketing.order.dto.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 목록 기간 필터 요청")
public record OrderPeriodFilterRequest(
	@Schema(description = "기간 조회 (개월)", example = "3")
	Integer months,

	@Schema(description = "조회 시작 날짜", example = "2026-01-01")
	LocalDate startDate,

	@Schema(description = "조회 종료 날짜", example = "2026-03-31")
	LocalDate endDate
) {
}
