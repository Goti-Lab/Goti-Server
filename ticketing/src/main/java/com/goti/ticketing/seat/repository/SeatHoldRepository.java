package com.goti.ticketing.seat.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goti.ticketing.constants.SeatHoldStatus;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHoldEntity, UUID>, SeatHoldRepositoryCustom {
	@Query("""
		SELECT sh
			FROM SeatHoldEntity sh
		WHERE sh.status = :status
		  AND sh.expiredAt < :now
		ORDER BY sh.expiredAt ASC
	""")
	List<SeatHoldEntity> findExpiredHolds(
		@Param("status") SeatHoldStatus status,
		@Param("now") LocalDateTime now,
		Pageable pageable
	);
}
