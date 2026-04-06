package com.goti.ticketing.game.service.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	@Transactional(readOnly = true)
	public List<GameScheduleSearchResponse> searchSchedules(GameScheduleSearchCondition condition) {
		List<GameScheduleSearchResponse> schedules = gameScheduleRepository.searchSchedules(condition);

		if (schedules.isEmpty())
			return schedules;

		Set<UUID> teamIds = schedules.stream()
			.flatMap(s -> Stream.of(s.homeTeamId(), s.awayTeamId()))
			.filter(java.util.Objects::nonNull)
			.collect(Collectors.toSet());

		Set<UUID> stadiumIds = schedules.stream()
			.map(GameScheduleSearchResponse::stadiumId)
			.filter(java.util.Objects::nonNull)
			.collect(Collectors.toSet());

		Map<UUID, String> teamDisplayNameMap = getBaseballTeamDisplayNamesMap(teamIds);
		Map<UUID, String> stadiumLocationMap = getStadiumLocationsMap(stadiumIds);

		return schedules.stream().map(
			schedule -> schedule.withExternalInfo(
				teamDisplayNameMap.get(schedule.homeTeamId()),
				teamDisplayNameMap.get(schedule.awayTeamId()),
				stadiumLocationMap.get(schedule.stadiumId())
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
		GameScheduleSearchResponse schedule = gameScheduleRepository.findScheduleByGameId(gameId)
			.orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

		Map<UUID, String> teamNames = getBaseballTeamDisplayNamesMap(
			Set.of(schedule.homeTeamId(), schedule.awayTeamId())
		);
		Map<UUID, String> stadiumLocations = getStadiumLocationsMap(
			Set.of(schedule.stadiumId())
		);

		return schedule.withExternalInfo(
			teamNames.get(schedule.homeTeamId()),
			teamNames.get(schedule.awayTeamId()),
			stadiumLocations.get(schedule.stadiumId())
		);
	}
}
