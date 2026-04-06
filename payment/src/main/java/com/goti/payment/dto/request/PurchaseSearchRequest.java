package com.goti.payment.dto.request;

import java.time.LocalDate;

import org.springdoc.core.annotations.ParameterObject;

import com.goti.payment.dto.request.enums.PurchaseSearchType;

import io.swagger.v3.oas.annotations.media.Schema;

@ParameterObject
@Schema(description = "구매 내역 통합 조회 요청")
public record PurchaseSearchRequest(
	@Schema(description = "구매 내역 조회 타입", example = "ALL")
    PurchaseSearchType type,

	@Schema(description = "주문 번호, 경기명, 좌석 정보 검색", example = "두산")
	String keyword,

	@Schema(description = "기간 조회 (개월)", example = "3")
	Integer months,

	@Schema(description = "조회 시작 날짜", example = "2026-01-01")
	LocalDate startDate,

	@Schema(description = "조회 종료 날짜", example = "2026-03-31")
	LocalDate endDate
) {
}
