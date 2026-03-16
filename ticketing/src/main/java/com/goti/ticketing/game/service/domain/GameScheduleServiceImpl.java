package com.goti.ticketing.game.service.domain;

import com.goti.ticketing.constants.LeagueType;
import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;

import com.goti.ticketing.game.repository.gameschedule.GameScheduleRepository;

import com.goti.global.validation.Preconditions;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameScheduleServiceImpl implements GameScheduleService {

	private final GameScheduleRepository gameScheduleRepository;

	@Override
	@Transactional
	public GameScheduleEntity create(
		UUID homeTeamId,
		UUID awayTeamId,
		UUID stadiumId,
		LocalDateTime startAt,
		LeagueType leagueType
	) {
		boolean isExists = gameScheduleRepository.existsDuplicateSchedule(
			homeTeamId, awayTeamId, startAt
		);

		Preconditions.validate(!isExists, ErrorCode.GAME_ALREADY_EXISTS);
		GameScheduleEntity gameSchedule = GameScheduleEntity.create(
			homeTeamId, awayTeamId, stadiumId, startAt, leagueType
		);

		return gameScheduleRepository.save(gameSchedule);
	}
}
