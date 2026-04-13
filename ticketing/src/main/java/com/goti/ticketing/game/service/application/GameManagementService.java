package com.goti.ticketing.game.service.application;

import com.goti.ticketing.constants.LeagueType;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.game.GameStatusEntity;
import com.goti.ticketing.domain.entity.game.GameTicketingStatusEntity;
import com.goti.ticketing.game.dto.response.GameCreateResponse;
import com.goti.ticketing.game.service.domain.GameScheduleService;

import com.goti.ticketing.game.service.domain.GameStatusService;

import com.goti.ticketing.game.service.domain.GameTicketingStatusService;

import com.goti.ticketing.infra.api.StadiumClient;

import com.goti.ticketing.infra.api.dto.response.StadiumTotalSeatsResponse;
import com.goti.ticketing.seat.service.domain.GameSeatSummaryService;

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
	private final StadiumClient stadiumClient;
	private final GameSeatSummaryService gameSeatSummaryService;

	public GameCreateResponse register(
		UUID homeTeamId,
		UUID awayTeamId,
		UUID stadiumId,
		LocalDateTime startAt,
		LeagueType leagueType
	) {
		validateBaseballTeam(homeTeamId);
		validateBaseballTeam(awayTeamId);

		StadiumTotalSeatsResponse totalSeatsResponse = stadiumClient.getStadiumTotalSeats(stadiumId);
		int totalSeats = totalSeatsResponse.totalSeats();

		return createGameSchedule(homeTeamId, awayTeamId, stadiumId, startAt, leagueType, totalSeats);
	}

	@Transactional
	public GameCreateResponse createGameSchedule(
		UUID homeTeamId,
		UUID awayTeamId,
		UUID stadiumId,
		LocalDateTime startAt,
		LeagueType leagueType,
		int totalSeats
	) {
		GameScheduleEntity gameSchedule = gameScheduleService.create(
			homeTeamId, awayTeamId, stadiumId, startAt, leagueType
		);

		GameStatusEntity gameStatus = gameStatusService.create(gameSchedule);
		GameTicketingStatusEntity gameTicketingStatus = gameTicketingStatusService.create(gameSchedule);

		gameSchedule.initGameStatus(gameStatus);
		gameSchedule.initTicketingStatus(gameTicketingStatus);

		gameSeatSummaryService.create(gameSchedule, totalSeats);

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
		stadiumClient.validateBaseballTeam(teamId);
	}
}
