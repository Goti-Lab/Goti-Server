package com.goti.queue.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "queue_state_snapshots")
@NoArgsConstructor(access = PROTECTED)
public class QueueStateSnapshotEntity {

	@Id
	@Column(nullable = false, updatable = false)
	private UUID gameId;

	@Column(nullable = false)
	private long maxCapacity;

	@Column(nullable = false)
	private long activeCount;

	@Column(nullable = false)
	private long publishedRank;

	@Column(nullable = false)
	private long currentAllowedRank;

	@Column(nullable = false)
	private long lastEnteredRank;

	@Column(nullable = false)
	private long sequence;

	@Column(nullable = false)
	private Instant capturedAt;

	private QueueStateSnapshotEntity(
		UUID gameId,
		long maxCapacity,
		long activeCount,
		long publishedRank,
		long currentAllowedRank,
		long lastEnteredRank,
		long sequence,
		Instant capturedAt
	) {
		this.gameId = gameId;
		this.maxCapacity = maxCapacity;
		this.activeCount = activeCount;
		this.publishedRank = publishedRank;
		this.currentAllowedRank = currentAllowedRank;
		this.lastEnteredRank = lastEnteredRank;
		this.sequence = sequence;
		this.capturedAt = capturedAt;
	}

	public static QueueStateSnapshotEntity create(
		UUID gameId,
		long maxCapacity,
		long activeCount,
		long publishedRank,
		long currentAllowedRank,
		long lastEnteredRank,
		long sequence,
		Instant capturedAt
	) {
		return new QueueStateSnapshotEntity(
			gameId,
			maxCapacity,
			activeCount,
			publishedRank,
			currentAllowedRank,
			lastEnteredRank,
			sequence,
			capturedAt
		);
	}

	public void update(
		long maxCapacity,
		long activeCount,
		long publishedRank,
		long currentAllowedRank,
		long lastEnteredRank,
		long sequence,
		Instant capturedAt
	) {
		this.maxCapacity = maxCapacity;
		this.activeCount = activeCount;
		this.publishedRank = publishedRank;
		this.currentAllowedRank = currentAllowedRank;
		this.lastEnteredRank = lastEnteredRank;
		this.sequence = sequence;
		this.capturedAt = capturedAt;
	}
}
