package com.goti.resale.dto.request;

import java.time.LocalDate;
import java.util.UUID;

import com.goti.resale.constants.ResaleSalesStatus;
import com.goti.resale.service.domain.command.ResaleSalesSearchCommand;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리셀 판매 내역 조회 요청")
public record ResaleSalesSearchRequest(
	@Schema(description = "기간 조회 (개월)", example = "3")
	Integer months,

	@Schema(description = "조회 시작 날짜", example = "2026-01-01")
	LocalDate startDate,

	@Schema(description = "조회 종료 날짜", example = "2026-03-31")
	LocalDate endDate,

	@Schema(description = "판매 상태 (ALL, LISTING, PENDING, SETTLED, CANCELED)", example = "ALL")
	ResaleSalesStatus status,

	@Schema(description = "페이지 번호 (0부터 시작)", example = "0")
	Integer page,

	@Schema(description = "페이지 크기", example = "5")
	Integer size
) {
	public ResaleSalesSearchRequest {
		if (page == null) page = 0;
		if (size == null) size = 5;
		if (status == null) status = ResaleSalesStatus.ALL;
	}

	public ResaleSalesSearchCommand toCommand(UUID sellerId) {
		return new ResaleSalesSearchCommand(
			sellerId,
			months,
			startDate,
			endDate,
			status,
			page,
			size
		);
	}
}
