package com.goti.ticketing.seat.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.goti.ticketing.domain.entity.game.GameScheduleEntity;

import com.goti.ticketing.domain.entity.seat.SeatEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;
import com.goti.ticketing.constants.SeatStatus;

@Repository
public interface SeatStatusRepository extends JpaRepository<SeatStatusEntity, UUID> {

	interface SectionAvailableSeatCountProjection {
		UUID getSectionId();
		Long getAvailableSeatCount();
	}

	@Query("""
		SELECT ss
			FROM SeatStatusEntity ss
		WHERE ss.game.id = :gameId
  		AND ss.seat.seatSection.id = :sectionId
	""")
	List<SeatStatusEntity> findSeatStatuses(
		@Param("gameId") UUID gameId,
		@Param("sectionId") UUID sectionId
	);

	Optional<SeatStatusEntity> findByGameAndSeat(GameScheduleEntity game, SeatEntity seat);

	@Query("""
		SELECT ss.seat.seatSection.id AS sectionId, COUNT(ss) AS availableSeatCount
		FROM SeatStatusEntity ss
		WHERE ss.game.id = :gameId
		  AND ss.seat.seatSection.id IN :sectionIds
		  AND ss.status = :status
		GROUP BY ss.seat.seatSection.id
	""")
	List<SectionAvailableSeatCountProjection> countSectionAvailableSeats(
		@Param("gameId") UUID gameId,
		@Param("sectionIds") List<UUID> sectionIds,
		@Param("status") SeatStatus status
	);
}
