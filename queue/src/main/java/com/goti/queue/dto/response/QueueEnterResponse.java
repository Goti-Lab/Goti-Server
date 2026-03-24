package com.goti.queue.dto.response;

import java.time.Instant;
import java.util.UUID;

public record QueueEnterResponse(
	String queueToken,
	long queueNumber,
	UUID gameId,
	Instant issuedAt
) {
}
