package com.goti.game.repository;

import com.goti.domain.entity.game.GameStatusEntity;

import com.goti.domain.entity.game.GameScheduleEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface GameStatusRepository extends JpaRepository<GameStatusEntity, UUID> {
	Optional<GameStatusEntity> findByGameSchedule(GameScheduleEntity gameSchedule);
}
