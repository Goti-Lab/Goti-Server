package com.goti.utils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.goti.service.ResaleListingService;
import com.goti.service.TicketService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResaleScheduler {

	private final TicketService ticketService;
	private final ResaleListingService listingService;

	@Scheduled(cron = "0 0 14,15,18,19 * * *")
	@Scheduled(cron = "0 30 19 * * *")
	public void autoCancelExpiredListings() {
		LocalDateTime threshold = LocalDateTime.now().minusHours(1);

		List<UUID> expiredGameIds = ticketService.getExpiredGameIds(threshold);

		// TODO : 아래 for문을 batch 나 event로 변경할 것
		for (UUID gameId : expiredGameIds) {
			try {
				listingService.cancelListingCauseGameStart(gameId);
			} catch (Exception ignored) {
			}
		}
	}
}
