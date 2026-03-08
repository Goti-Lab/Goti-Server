package com.goti.game.repository;

import com.goti.domain.entity.game.BaseballGameStatusEntity;

import com.goti.domain.entity.game.GameScheduleEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface BaseballGameStatusRepository extends JpaRepository<BaseballGameStatusEntity, UUID> {
	Optional<BaseballGameStatusEntity> findByGameSchedule(GameScheduleEntity gameSchedule);
}
