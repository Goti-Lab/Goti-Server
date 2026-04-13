package com.goti.ticketing.game.service.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.goti.ticketing.game.dto.response.repository.GameScheduleQueryModel;

import com.goti.ticketing.seat.service.application.GameSeatSummaryQueryService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.ticketing.game.dto.request.GameScheduleSearchCondition;
import com.goti.ticketing.game.dto.response.GameScheduleSearchResponse;
import com.goti.ticketing.game.repository.gameschedule.GameScheduleRepository;
import com.goti.ticketing.infra.api.StadiumApiClient;
import com.goti.ticketing.infra.api.dto.response.BaseballTeamDisplayNameResponse;
import com.goti.ticketing.infra.api.dto.response.StadiumLocationResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameScheduleSearchService {
	private final GameScheduleRepository gameScheduleRepository;
	private final StadiumApiClient stadiumApiClient;
	private final GameSeatSummaryQueryService gameSeatSummaryQueryService;

	@Transactional(readOnly = true)
	public List<GameScheduleSearchResponse> searchSchedules(GameScheduleSearchCondition condition) {
		List<GameScheduleQueryModel> queryModels = gameScheduleRepository.searchSchedules(condition);

		if (queryModels.isEmpty()) return List.of();

		List<UUID> gameIds = queryModels.stream().map(GameScheduleQueryModel::gameId).toList();

		Set<UUID> teamIds = queryModels.stream()
			.flatMap(
				queryModel -> Stream.of(queryModel.homeTeamId(), queryModel.awayTeamId())
			)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		Set<UUID> stadiumIds = queryModels.stream()
			.map(GameScheduleQueryModel::stadiumId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		Map<UUID, String> teamDisplayNameMap = getBaseballTeamDisplayNamesMap(teamIds);
		Map<UUID, String> stadiumLocationMap = getStadiumLocationsMap(stadiumIds);
		Map<UUID, Integer> seatCountMap = gameSeatSummaryQueryService.getRemainingSeatCounts(gameIds);

		return queryModels.stream().map(
			queryModel -> GameScheduleSearchResponse.from(
				queryModel,
				teamDisplayNameMap.get(queryModel.homeTeamId()),
				teamDisplayNameMap.get(queryModel.awayTeamId()),
				stadiumLocationMap.get(queryModel.stadiumId()),
				seatCountMap.getOrDefault(queryModel.gameId(), 0).longValue()
			)
		).toList();
	}

	private Map<UUID, String> getBaseballTeamDisplayNamesMap(Set<UUID> teamIds) {
		return stadiumApiClient
			.getBaseballTeamDisplayNames(new ArrayList<>(teamIds))
			.stream()
			.collect(Collectors.toMap(
				BaseballTeamDisplayNameResponse::teamId,
				BaseballTeamDisplayNameResponse::teamDisplayName
			));
	}

	private Map<UUID, String> getStadiumLocationsMap(Set<UUID> stadiumIds) {
		return stadiumApiClient
			.getStadiumLocations(new ArrayList<>(stadiumIds))
			.stream()
			.collect(Collectors.toMap(
				StadiumLocationResponse::stadiumId,
				StadiumLocationResponse::stadiumLocation
			));
	}

	@Transactional(readOnly = true)
	public GameScheduleSearchResponse getGameSchedule(UUID gameId) {
		GameScheduleQueryModel queryModel = gameScheduleRepository.findScheduleByGameId(gameId)
			.orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

		Map<UUID, String> teamNames = getBaseballTeamDisplayNamesMap(
			Set.of(queryModel.homeTeamId(), queryModel.awayTeamId())
		);
		Map<UUID, String> stadiumLocations = getStadiumLocationsMap(
			Set.of(queryModel.stadiumId())
		);

		List<UUID> gameIds = List.of(gameId);
		Map<UUID, Integer> seatCountMap = gameSeatSummaryQueryService.getRemainingSeatCounts(gameIds);
		return GameScheduleSearchResponse.from(
			queryModel,
			teamNames.get(queryModel.homeTeamId()),
			teamNames.get(queryModel.awayTeamId()),
			stadiumLocations.get(queryModel.stadiumId()),
			seatCountMap.getOrDefault(queryModel.gameId(), 0).longValue()
		);
	}
}
