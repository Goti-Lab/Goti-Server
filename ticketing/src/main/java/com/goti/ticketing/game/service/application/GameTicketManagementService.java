package com.goti.ticketing.game.service.application;

import com.goti.ticketing.constants.TicketingStatus;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.game.GameTicketingStatusEntity;
import com.goti.ticketing.game.service.domain.GameTicketingStatusService;
import com.goti.ticketing.seat.service.domain.SeatStatusService;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameTicketManagementService {
	private final GameTicketingStatusService gameTicketingStatusService;
	private final SeatStatusService seatStatusService;

	public void processSoldout(GameScheduleEntity gameSchedule) {
		GameTicketingStatusEntity ticketingStatus = gameTicketingStatusService.getLocked(gameSchedule);

		if (ticketingStatus == null || ticketingStatus.isExhausted()) {
			return;
		}

		if (seatStatusService.countAvailableSeats(gameSchedule) == 0) {
			ticketingStatus.updateStatus(TicketingStatus.EXHAUSTED);
		}
	}

	public void processRestoreAvailable(GameScheduleEntity gameSchedule) {
		GameTicketingStatusEntity ticketingStatus = gameTicketingStatusService.getLocked(gameSchedule);

		if (ticketingStatus == null || !ticketingStatus.isExhausted()) {
			return;
		}

		if (seatStatusService.countAvailableSeats(gameSchedule) > 0) {
			ticketingStatus.updateStatus(TicketingStatus.AVAILABLE);
		}
	}
}
