package com.goti.queue.dto.response;

import java.util.UUID;

import com.goti.queue.constants.QueueStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대기열 최종 입장 응답")
public record QueueSeatEnterResponse(
	@Schema(description = "경기 ID", example = "b7c4b0b2-7d4f-4d58-a6ab-111111111111")
	UUID gameId,

	@Schema(description = "최종 입장 허용 여부", example = "true")
	boolean enterAllowed,

	@Schema(description = "최종 입장 처리된 대기 순번", example = "2100")
	long queueNumber,

	@Schema(description = "입장 후 대기열 상태", example = "ADMITTED")
	QueueStatus status
) {
}
