package com.goti.game.service;

import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.game.BaseballGameEntity;
import com.goti.domain.entity.game.BaseballGameStatusEntity;
import com.goti.game.dto.response.GameResponse;
import com.goti.game.repository.BaseballGameRepository;
import com.goti.game.repository.BaseballGameStatusRepository;

import com.goti.game.service.command.CreateGameCommand;

import com.goti.global.validation.Preconditions;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BaseballGameApplicationService {
	private final BaseballGameRepository baseballGameRepository;
	private final BaseballGameStatusRepository baseballGameStatusRepository;

	@Transactional
	public GameResponse create(CreateGameCommand cmd) {
		boolean exists = baseballGameRepository.existsByHomeTeamIdAndAwayTeamIdAndPlayDateAndStartAt(
			cmd.homeTeamId(),
			cmd.awayTeamId(),
			cmd.playDate(),
			cmd.startAt()
		);

		Preconditions.validate(!exists, ErrorCode.GAME_ALREADY_EXISTS);

		BaseballGameEntity game = BaseballGameEntity.create(
			cmd.homeTeamId(),
			cmd.awayTeamId(),
			cmd.stadiumId(),
			cmd.playDate(),
			cmd.startAt(),
			cmd.reservationOpenedAt(),
			cmd.reservationClosedAt()
		);
		BaseballGameEntity savedGame = baseballGameRepository.save(game);

		BaseballGameStatusEntity gameStatusEntity = BaseballGameStatusEntity.init(savedGame);
		baseballGameStatusRepository.save(gameStatusEntity);

		return GameResponse.from(savedGame);
	}
}
