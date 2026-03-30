package com.goti.resale.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.goti.resale.infra.TicketClient;
import com.goti.resale.infra.dto.TicketGameInfo;
import com.goti.resale.service.application.ResaleListingProcessService;
import com.goti.resale.service.application.ResalePriceProcessService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResaleScheduler {

	private final TicketClient ticketClient;
	private final ResaleListingProcessService listingService;
	private final ResalePriceProcessService priceService;

	@Scheduled(cron = "0 0 14,15,18,19 * * *")
	@Scheduled(cron = "0 30 19 * * *")
	public void autoCancelExpiredListings() {
		LocalDateTime threshold = LocalDateTime.now().minusHours(1);
		List<UUID> expiredGameIds = ticketClient.getExpiredGameIds(threshold);

		if (!expiredGameIds.isEmpty()) {
			try {
				listingService.cancelListingsByGameIds(expiredGameIds);
			} catch (Exception e) {
				log.error("경기 시작에 따른 리셀 일괄 취소 실패", e);
			}
		}
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void updateDailyBasePrices() {
		List<TicketGameInfo> upcomingGames = ticketClient.getUpcomingGames();
		for (TicketGameInfo info : upcomingGames) {
			try {
				priceService.updateDailyBasePrice(info.gameId(), info.gradeId());
			} catch (Exception e) {
				log.error("기준가 업데이트 실패 - gameId: {}, gradeId: {}",
					info.gameId(), info.gradeId(), e);
			}
		}
	}
}

