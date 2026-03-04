package com.goti.game.repository;

import com.goti.domain.entity.game.BaseballGameEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Repository
public interface BaseballGameRepository extends JpaRepository<BaseballGameEntity, UUID> {
	boolean existsByHomeTeamIdAndAwayTeamIdAndPlayDateAndStartAt(
		UUID homeTeamId,
		UUID awayTeamId,
		LocalDate playDate,
		LocalTime startAt
	);
}
