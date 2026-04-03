package com.goti.resale.dto.request;

import java.time.LocalDate;

import com.goti.resale.constants.ResaleOrderSearchStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리셀 판매 내역 조회 요청")
public record ResaleSearchSalesRequest(
	@Schema(description = "기간 조회 개월 수", example = "3")
	Integer months,

	@Schema(description = "조회 시작 날짜", example = "2026-01-01")
	LocalDate startDate,

	@Schema(description = "조회 종료 날짜", example = "2026-03-31")
	LocalDate endDate,

	@Schema(description = "판매 상태", example = "ALL")
	ResaleOrderSearchStatus status
) {
}
