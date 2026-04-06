package com.goti.queue.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record QueueEnterRequest(
	@NotNull UUID gameId
) {
}
