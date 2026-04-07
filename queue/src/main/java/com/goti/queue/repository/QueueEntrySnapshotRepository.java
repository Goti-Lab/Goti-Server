package com.goti.queue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.queue.domain.entity.QueueEntrySnapshotEntity;
import com.goti.queue.domain.entity.QueueEntrySnapshotId;

@Repository
public interface QueueEntrySnapshotRepository extends JpaRepository<QueueEntrySnapshotEntity, QueueEntrySnapshotId> {
}
