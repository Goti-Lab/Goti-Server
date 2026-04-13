package com.goti.ticketing.seat.service.domain;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.ticketing.domain.entity.seat.GameSeatSummaryEntity;
import com.goti.ticketing.seat.repository.GameSeatSummaryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GameSeatSummaryServiceImpl implements GameSeatSummaryService {

	private final GameSeatSummaryRepository gameSeatSummaryRepository;

	@Override
	@Transactional(readOnly = true)
	public List<GameSeatSummaryEntity> getRemainingSeatCounts(List<UUID> gameIds) {
		return gameSeatSummaryRepository.findAllByGameScheduleIdIn(gameIds);
	}

	@Override
	@Transactional
	public void decrease(UUID gameId, int count) {
		int affectedRows = gameSeatSummaryRepository.decreaseAvailableCount(gameId, count);
		if (affectedRows == 0) {
			throw new CustomException(ErrorCode.SEAT_INVENTORY_EXHAUSTED);
		}
	}

	@Override
	@Transactional
	public void increase(UUID gameId, int count) {
		int affectedRows = gameSeatSummaryRepository.increaseAvailableCount(gameId, count);
		if (affectedRows == 0) {
			throw new CustomException(ErrorCode.SEAT_INVENTORY_EXCEEDED);
		}
	}
}
