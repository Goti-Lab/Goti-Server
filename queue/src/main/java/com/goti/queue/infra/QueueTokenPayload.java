package com.goti.queue.infra;

import java.time.Instant;
import java.util.UUID;

public record QueueTokenPayload(
	UUID tokenId,
	UUID gameId,
	UUID userId,
	long queueNumber,
	Instant issuedAt
) {
}
