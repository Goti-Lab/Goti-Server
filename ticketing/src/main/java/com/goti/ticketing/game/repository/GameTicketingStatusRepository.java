package com.goti.ticketing.game.repository;

import com.goti.ticketing.constants.TicketingStatus;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.game.GameTicketingStatusEntity;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GameTicketingStatusRepository extends JpaRepository<GameTicketingStatusEntity, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<GameTicketingStatusEntity> findByGameSchedule(GameScheduleEntity gameSchedule);

	@Query(
		"SELECT ticketing_status " +
			"FROM GameTicketingStatusEntity ticketing_status " +
		 "WHERE ticketing_status.status = :status " +
			 "AND ticketing_status.ticketingOpenedAt <= :now"
	)
	List<GameTicketingStatusEntity> findOpenableSchedules(
		@Param("status") TicketingStatus status,
		@Param("now") LocalDateTime now
	);

	@Query(
		"SELECT ticketing_status " +
			"FROM GameTicketingStatusEntity ticketing_status " +
		 "WHERE ticketing_status.status " +
			  "IN :statuses " +
			 "AND ticketing_status.ticketingEndAt <= :now ")
	List<GameTicketingStatusEntity> findTerminatableSchedules(
		@Param("statuses") List<TicketingStatus> statuses,
		@Param("now") LocalDateTime now
	);
}
