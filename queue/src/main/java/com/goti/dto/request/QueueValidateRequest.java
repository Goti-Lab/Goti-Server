package com.goti.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record QueueValidateRequest(
	@NotNull(message = "게임ID는 필수 항목입니다.")
	UUID gameId
) {
}
