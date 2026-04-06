package com.goti.queue.dto.response;

import java.util.UUID;

import com.goti.queue.constants.QueueStatus;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대기열 이탈 응답")
public record QueueLeaveResponse(
	@Schema(description = "경기 ID", example = "b7c4b0b2-7d4f-4d58-a6ab-111111111111")
	UUID gameId,

	@Schema(description = "현재 수용 인원 해제 여부", example = "true")
	boolean released,

	@Schema(description = "이탈 후 대기열 상태", example = "LEFT")
	QueueStatus status
) {
}
