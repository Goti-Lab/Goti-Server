package com.goti.queue.domain;

import java.io.Serializable;
import java.time.Instant;

import com.goti.queue.constants.QueueStatus;

public record QueueEntry(
	long queueNumber,
	Instant issuedAt,
	QueueStatus status
) implements Serializable {
}
