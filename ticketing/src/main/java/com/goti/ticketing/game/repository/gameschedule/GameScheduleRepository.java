package com.goti.ticketing.game.repository.gameschedule;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.goti.ticketing.domain.entity.game.GameScheduleEntity;

@Repository
public interface GameScheduleRepository extends JpaRepository<GameScheduleEntity, UUID>,
	GameScheduleRepositoryCustom {

	List<GameScheduleEntity> findAllByStartAtBefore(LocalDateTime threshold);

	List<GameScheduleEntity> findAllByStartAtAfter(LocalDateTime threshold);
}
