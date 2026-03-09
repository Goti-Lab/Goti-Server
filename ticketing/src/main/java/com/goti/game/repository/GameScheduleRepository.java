package com.goti.game.repository;

import com.goti.domain.entity.game.GameScheduleEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Repository
public interface GameScheduleRepository extends JpaRepository<GameScheduleEntity, UUID> {
	boolean existsByHomeTeamIdAndAwayTeamIdAndPlayDateAndStartAt(
		UUID homeTeamId,
		UUID awayTeamId,
		LocalDate playDate,
		LocalTime startAt
	);
}
