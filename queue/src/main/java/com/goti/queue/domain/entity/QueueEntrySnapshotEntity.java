package com.goti.queue.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import com.goti.queue.constants.QueueStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "queue_entry_snapshots")
@NoArgsConstructor(access = PROTECTED)
public class QueueEntrySnapshotEntity {
	// TODO: kafka로 PostgreSQL snapshot 동기화
	@EmbeddedId
	private QueueEntrySnapshotId id;

	@Lob
	@Column(nullable = false)
	private String queueToken;

	@Column(nullable = false)
	private long queueNumber;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private QueueStatus status;

	@Column(nullable = false)
	private Instant capturedAt;

	private QueueEntrySnapshotEntity(
		QueueEntrySnapshotId id,
		String queueToken,
		long queueNumber,
		QueueStatus status,
		Instant capturedAt
	) {
		this.id = id;
		this.queueToken = queueToken;
		this.queueNumber = queueNumber;
		this.status = status;
		this.capturedAt = capturedAt;
	}

	public static QueueEntrySnapshotEntity create(
		UUID gameId,
		UUID userId,
		String queueToken,
		long queueNumber,
		QueueStatus status,
		Instant capturedAt
	) {
		return new QueueEntrySnapshotEntity(
			QueueEntrySnapshotId.of(gameId, userId),
			queueToken,
			queueNumber,
			status,
			capturedAt
		);
	}

	public void update(
		String queueToken,
		long queueNumber,
		QueueStatus status,
		Instant capturedAt
	) {
		this.queueToken = queueToken;
		this.queueNumber = queueNumber;
		this.status = status;
		this.capturedAt = capturedAt;
	}

	public UUID getGameId() {
		return id.getGameId();
	}

	public UUID getUserId() {
		return id.getUserId();
	}
}
