package com.goti.game.service.domain;

import com.goti.constants.TicketingStatus;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.game.GameTicketingStatusEntity;

import com.goti.game.repository.GameTicketingStatusRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GameTicketingStatusServiceImpl implements GameTicketingStatusService {

	private final GameTicketingStatusRepository gameTicketingStatusRepository;

	@Override
	@Transactional
	public GameTicketingStatusEntity create(GameScheduleEntity gameSchedule) {
		LocalDateTime gameStartAt = gameSchedule.getStartAt();
		LocalDateTime now = LocalDateTime.now();

		LocalDateTime openedAt = calculateOpenedAt(gameStartAt, now);
		LocalDateTime endAt = gameStartAt.plusHours(1).withSecond(0).withNano(0);
		TicketingStatus status = TicketingStatus.SCHEDULED;
		if (openedAt.isBefore(now) || openedAt.isEqual(now))
			status = TicketingStatus.AVAILABLE;

		GameTicketingStatusEntity gameTicketingStatus = GameTicketingStatusEntity.create(
			gameSchedule, openedAt, endAt, status
		);
		return gameTicketingStatusRepository.save(gameTicketingStatus);
	}

	private LocalDateTime calculateOpenedAt(LocalDateTime gameStartAt, LocalDateTime now) {
		LocalDateTime standardOpenAt = gameStartAt.minusDays(7)
			.withHour(11)
			.withMinute(0)
			.withSecond(0)
			.withNano(0);

		if (standardOpenAt.isBefore(now)) {
			standardOpenAt = now.toLocalDate().atTime(11, 0, 0, 0);
		}

		return standardOpenAt;
	}
}
