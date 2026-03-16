package com.goti.ticketing.game.service.application;

import com.goti.ticketing.constants.LeagueType;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.game.GameStatusEntity;
import com.goti.ticketing.domain.entity.game.GameTicketingStatusEntity;
import com.goti.ticketing.game.dto.response.GameCreateResponse;
import com.goti.ticketing.game.service.domain.GameScheduleService;

import com.goti.ticketing.game.service.domain.GameStatusService;

import com.goti.ticketing.game.service.domain.GameTicketingStatusService;
import com.goti.stadium.service.domain.baseballteam.BaseballTeamService;

import com.goti.stadium.service.domain.stadium.StadiumService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameManagementService {

	private final GameScheduleService gameScheduleService;
	private final GameStatusService gameStatusService;
	private final GameTicketingStatusService gameTicketingStatusService;
	private final BaseballTeamService baseballTeamService;
	private final StadiumService stadiumService;

	@Transactional
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

		GameTicketingStatusEntity gameTicketingStatus =
			gameTicketingStatusService.create(gameSchedule);

		return GameCreateResponse.from(
			gameSchedule.getId(),
			gameSchedule.getHomeTeamId(),
			gameSchedule.getAwayTeamId(),
			gameSchedule.getStadiumId(),
			gameSchedule.getStartAt(),
			gameSchedule.getLeagueType(),
			gameStatus.getGameStatus(),
			gameTicketingStatus.getTicketingOpenedAt(),
			gameTicketingStatus.getTicketingEndAt(),
			gameTicketingStatus.getStatus()
		);
	}

	private void validateBaseballTeam(UUID teamId) {
		baseballTeamService.getById(teamId);
	}

	private void validateStadium(UUID stadiumId) {
		stadiumService.getById(stadiumId);
	}
}
