package com.goti.ticketing.seat.service.application;

import com.goti.ticketing.domain.entity.seat.GameSeatSummaryEntity;
import com.goti.ticketing.seat.service.domain.GameSeatSummaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameSeatSummaryQueryService {

	private final GameSeatSummaryService gameSeatSummaryService;

	@Transactional(readOnly = true)
	public Map<UUID, Integer> getRemainingSeatCounts(List<UUID> gameIds) {
		if (gameIds == null || gameIds.isEmpty()) {
			return Collections.emptyMap();
		}
		List<GameSeatSummaryEntity> summaries = gameSeatSummaryService.getRemainingSeatCounts(gameIds);

		return summaries.stream()
			.collect(Collectors.toMap(
				summary -> summary.getGameSchedule().getId(),
				GameSeatSummaryEntity::getAvailableCount
			));
	}
}
