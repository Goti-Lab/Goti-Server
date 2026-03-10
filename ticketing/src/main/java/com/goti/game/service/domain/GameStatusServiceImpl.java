package com.goti.game.service.domain;

import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.game.GameStatusEntity;
import com.goti.game.repository.GameStatusRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameStatusServiceImpl implements GameStatusService {

	private final GameStatusRepository gameStatusRepository;

	@Override
	@Transactional
	public GameStatusEntity create(GameScheduleEntity gameSchedule) {
		GameStatusEntity gameStatus = GameStatusEntity.create(gameSchedule);
		return gameStatusRepository.save(gameStatus);
	}

}
