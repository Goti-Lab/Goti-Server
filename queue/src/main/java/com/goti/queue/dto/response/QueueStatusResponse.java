package com.goti.queue.dto.response;

import java.time.Instant;
import java.util.UUID;

public record QueueStatusResponse(
	UUID gameId,
	long maxCapacity,
	long activeCount,
	long availableSlots,
	long currentAllowedRank,
	long publishedRank,
	Instant updatedAt
) {
}
