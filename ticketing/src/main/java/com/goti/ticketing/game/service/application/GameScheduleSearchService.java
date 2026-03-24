package com.goti.ticketing.game.service.application;

import com.goti.ticketing.game.dto.request.GameScheduleSearchCondition;
import com.goti.ticketing.game.dto.response.GameScheduleSearchResponse;
import com.goti.ticketing.game.repository.gameschedule.GameScheduleRepository;

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
		return gameScheduleRepository.searchSchedules(condition);
	}

}
