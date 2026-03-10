package com.goti.game.repository.gameschedule;

import com.goti.domain.entity.game.GameScheduleEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GameScheduleRepository extends JpaRepository<GameScheduleEntity, UUID>,
	GameScheduleRepositoryCustom {

}
