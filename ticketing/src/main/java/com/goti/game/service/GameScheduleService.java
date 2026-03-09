package com.goti.game.service;

import com.goti.exception.CustomException;

import com.goti.game.repository.GameScheduleRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.game.BaseballGameStatusEntity;
import com.goti.game.dto.response.GameResponse;
import com.goti.game.repository.BaseballGameStatusRepository;
import com.goti.game.service.command.CreateGameCommand;
import com.goti.global.validation.Preconditions;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameScheduleService {
	private final GameScheduleRepository gameScheduleRepository;
	private final BaseballGameStatusRepository baseballGameStatusRepository;

	@Transactional
	public GameResponse create(CreateGameCommand cmd) {
		boolean exists = gameScheduleRepository.existsByHomeTeamIdAndAwayTeamIdAndPlayDateAndStartAt(
			cmd.homeTeamId(),
			cmd.awayTeamId(),
			cmd.playDate(),
			cmd.startAt()
		);

		Preconditions.validate(!exists, ErrorCode.GAME_ALREADY_EXISTS);

		GameScheduleEntity game = GameScheduleEntity.create(
			cmd.homeTeamId(),
			cmd.awayTeamId(),
			cmd.stadiumId(),
			cmd.playDate(),
			cmd.startAt()
		);
		GameScheduleEntity savedGame = gameScheduleRepository.save(game);

		BaseballGameStatusEntity gameStatus = BaseballGameStatusEntity.init(savedGame);
		baseballGameStatusRepository.save(gameStatus);

		return GameResponse.from(savedGame, gameStatus);
	}

	@Transactional(readOnly = true)
	public Page<GameResponse> getGames(Pageable pageable) {
		return gameScheduleRepository.findAll(pageable)
			.map(gameSchedule -> baseballGameStatusRepository.findByGameSchedule(gameSchedule)
				.map(status -> GameResponse.from(gameSchedule, status))
				.orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND)));
	}

	@Transactional(readOnly = true)
	public GameResponse getGame(UUID gameScheduleId) {
		GameScheduleEntity gameSchedule = gameScheduleRepository.findById(gameScheduleId)
			.orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

		BaseballGameStatusEntity status = baseballGameStatusRepository
			.findByGameSchedule(gameSchedule)
			.orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

		return GameResponse.from(gameSchedule, status);
	}
}
