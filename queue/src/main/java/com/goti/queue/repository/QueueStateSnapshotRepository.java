package com.goti.queue.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.queue.domain.entity.QueueStateSnapshotEntity;

@Repository
public interface QueueStateSnapshotRepository extends JpaRepository<QueueStateSnapshotEntity, UUID> {
}
