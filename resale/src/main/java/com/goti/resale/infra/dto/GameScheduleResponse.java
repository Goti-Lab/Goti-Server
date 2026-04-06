package com.goti.resale.infra.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "경기 일정 상세 응답")
@JsonIgnoreProperties(ignoreUnknown = true)
public record GameScheduleResponse(
	@Schema(description = "경기 식별 ID")
	UUID gameId,

	@Schema(description = "경기 시작 일시")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime startAt,

	@Schema(description = "예매 시작 일시")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime ticketingOpenedAt,

	@Schema(description = "예매 종료 일시")
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm")
	LocalDateTime ticketingEndAt
) {
}
