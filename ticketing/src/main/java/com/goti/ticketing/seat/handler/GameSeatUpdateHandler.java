package com.goti.ticketing.seat.handler;

import com.goti.ticketing.seat.service.domain.GameSeatSummaryService;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameSeatUpdateHandler {
	private final GameSeatSummaryService summaryService;

	@Transactional(propagation = Propagation.MANDATORY)
	public void onSeatDecrease(UUID gameScheduleId, int count) {
		log.info("[Inventory-Update] action=DECREASE, gameScheduleId={}, count={}", gameScheduleId, count);

		try {
			summaryService.decrease(gameScheduleId, count);
			log.debug("[Inventory-Update] Successfully decreased inventory for gameScheduleId={}", gameScheduleId);
		} catch (Exception e) {
			log.error("[Inventory-Update] Failed to decrease inventory for gameScheduleId={}. Error={}",
				gameScheduleId, e.getMessage());
			throw e;
		}
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void onSeatIncrease(UUID gameScheduleId, int count) {
		log.info("[Inventory-Update] action=INCREASE, gameScheduleId={}, count={}", gameScheduleId, count);

		try {
			summaryService.increase(gameScheduleId, count);
			log.debug("[Inventory-Update] Successfully increased inventory for gameScheduleId={}", gameScheduleId);
		} catch (Exception e) {
			log.error("[Inventory-Update] Failed to increase inventory for gameScheduleId={}. Error={}",
				gameScheduleId, e.getMessage());
			throw e;
		}
	}
}
