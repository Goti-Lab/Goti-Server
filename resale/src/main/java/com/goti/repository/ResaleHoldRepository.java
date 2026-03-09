package com.goti.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goti.constants.ResaleHoldStatus;
import com.goti.domain.entity.resale.ResaleHoldEntity;

public interface ResaleHoldRepository extends JpaRepository<ResaleHoldEntity, UUID> {

	Optional<ResaleHoldEntity> findByIdAndUserIdAndStatus(UUID id, UUID userId, ResaleHoldStatus status);

	@Query("SELECT r FROM ResaleHoldEntity r "
		+ "WHERE r.status = :status "
		+ "AND r.expiredAt < :now "
		+ "ORDER BY r.expiredAt ASC")
	List<ResaleHoldEntity> findExpiredResaleHolds(
		@Param("status") ResaleHoldStatus status,
		@Param("now") LocalDateTime now,
		Pageable pageable
	);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE ResaleHoldEntity r SET r.status = :newStatus, r.releasedAt = :now " +
		"WHERE r.id IN :holdIds")
	void updateStatusToReleased(
		@Param("holdIds") List<UUID> holdIds,
		@Param("newStatus") ResaleHoldStatus newStatus,
		@Param("now") LocalDateTime now
	);
}

