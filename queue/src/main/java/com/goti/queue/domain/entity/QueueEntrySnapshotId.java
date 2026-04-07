package com.goti.queue.domain.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED)
public class QueueEntrySnapshotId implements Serializable {
	// queue_entry_snapshots 테이블의 복합키 클래스

	@Column(nullable = false, updatable = false)
	private UUID gameId;

	@Column(nullable = false, updatable = false)
	private UUID userId;

	private QueueEntrySnapshotId(UUID gameId, UUID userId) {
		this.gameId = gameId;
		this.userId = userId;
	}

	public static QueueEntrySnapshotId of(UUID gameId, UUID userId) {
		return new QueueEntrySnapshotId(gameId, userId);
	}
}
