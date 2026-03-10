package com.goti.game.service.application;

import com.goti.constants.LeagueType;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.game.GameStatusEntity;
import com.goti.game.dto.response.GameCreateResponse;
import com.goti.game.service.domain.GameScheduleService;

import com.goti.game.service.domain.GameStatusService;

import com.goti.service.domain.baseballteam.BaseballTeamService;

import com.goti.service.domain.stadium.StadiumService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameOperationService {

	private final GameScheduleService gameScheduleService;
	private final GameStatusService gameStatusService;
	private final BaseballTeamService baseballTeamService;
	private final StadiumService stadiumService;

	public GameCreateResponse register(
		UUID homeTeamId,
		UUID awayTeamId,
		UUID stadiumId,
		LocalDateTime startAt,
		LeagueType leagueType
	) {

		validateBaseballTeam(homeTeamId);
		validateBaseballTeam(awayTeamId);
		validateStadium(stadiumId);

		GameScheduleEntity gameSchedule = gameScheduleService.create(
			homeTeamId, awayTeamId, stadiumId, startAt, leagueType
		);

		GameStatusEntity gameStatus = gameStatusService.create(gameSchedule);

		return GameCreateResponse.from(
			gameSchedule.getId(),
			gameSchedule.getHomeTeamId(),
			gameSchedule.getAwayTeamId(),
			gameSchedule.getStadiumId(),
			gameSchedule.getStartAt(),
			gameSchedule.getLeagueType(),
			gameStatus.getGameStatus()
		);
	}

	private void validateBaseballTeam(UUID teamId) {
		baseballTeamService.getById(teamId);
	}

	private void validateStadium(UUID stadiumId) {
		stadiumService.getById(stadiumId);
	}
}
