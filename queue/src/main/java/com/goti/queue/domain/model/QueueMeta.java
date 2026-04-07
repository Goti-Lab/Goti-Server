package com.goti.queue.domain.model;

import java.time.Instant;

public record QueueMeta(
	long maxCapacity,
	long activeCount,
	long publishedRank,
	long currentAllowedRank,
	long lastEnteredRank,
	Instant updatedAt
) {
}
