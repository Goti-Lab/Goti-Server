package com.goti.ticketing.seat.repository;

import com.goti.ticketing.domain.entity.seat.GameSeatSummaryEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GameSeatSummaryRepository extends JpaRepository<GameSeatSummaryEntity, UUID> {

	List<GameSeatSummaryEntity> findAllByGameScheduleIdIn(List<UUID> gameIds);

	@Modifying(clearAutomatically = true)
	@Query(
    """
			UPDATE GameSeatSummaryEntity summary
				 SET summary.availableCount = summary.availableCount + :count
			 WHERE summary.gameSchedule.id = :gameScheduleId
				 AND summary.availableCount + :count <= summary.totalCount
		"""
	)
	int increaseAvailableCount(
		@Param("gameScheduleId") UUID gameScheduleId,
		@Param("count") int count
	);

	@Modifying(clearAutomatically = true)
	@Query(
    """
			UPDATE GameSeatSummaryEntity summary
				 SET summary.availableCount = summary.availableCount - :count
     	 WHERE summary.gameSchedule.id = :gameScheduleId
       	 AND summary.availableCount >= :count
		"""
	)
	int decreaseAvailableCount(
		@Param("gameScheduleId") UUID gameScheduleId,
		@Param("count") int count
	);
}
