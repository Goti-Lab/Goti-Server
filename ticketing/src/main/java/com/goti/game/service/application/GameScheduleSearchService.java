package com.goti.game.service.application;

import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.game.dto.request.GameScheduleSearchCondition;
import com.goti.game.dto.response.GameScheduleSearchResponse;
import com.goti.game.repository.gameschedule.GameScheduleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameScheduleSearchService {
	private final GameScheduleRepository gameScheduleRepository;

	@Transactional(readOnly = true)
	public List<GameScheduleSearchResponse> searchSchedules(GameScheduleSearchCondition condition) {
		List<GameScheduleEntity> schedules = gameScheduleRepository.searchSchedules(condition);

		return schedules.stream().map(
			this::toGameScheduleSearchResponse
		).toList();
	}

	private GameScheduleSearchResponse toGameScheduleSearchResponse(GameScheduleEntity schedule) {
		return new GameScheduleSearchResponse(
			schedule.getId(),
			schedule.getStartAt(),
			schedule.getLeagueType(),
			schedule.getHomeTeamId(),
			schedule.getAwayTeamId(),
			schedule.getStadiumId(),
			schedule.getGameStatus().getGameStatus(),
			schedule.getGameStatus().getHomeTeamScore(),
			schedule.getGameStatus().getAwayTeamScore(),
			schedule.getGameStatus().getGameResult(),
			schedule.getTicketingStatus().getStatus(),
			schedule.getTicketingStatus().getTicketingOpenedAt()
		);
	}
}
