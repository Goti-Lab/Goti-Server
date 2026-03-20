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
import com.goti.ticketing.seat.repository.dto.SeatGradeAvailableSeatCount;

@Repository
public interface SeatStatusRepository extends JpaRepository<SeatStatusEntity, UUID> {

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
		SELECT new com.goti.ticketing.seat.repository.dto.SeatGradeAvailableSeatCount(
			ss.seat.seatSection.seatGrade.id,
			COUNT(ss)
		)
		FROM SeatStatusEntity ss
		WHERE ss.game.id = :gameId
		  AND ss.seat.seatSection.seatGrade.id IN :seatGradeIds
		  AND ss.status = :status
		GROUP BY ss.seat.seatSection.seatGrade.id
	""")
	List<SeatGradeAvailableSeatCount> countSeatGradeAvailableSeats(
		@Param("gameId") UUID gameId,
		@Param("seatGradeIds") List<UUID> seatGradeIds,
		@Param("status") SeatStatus status
	);
}
