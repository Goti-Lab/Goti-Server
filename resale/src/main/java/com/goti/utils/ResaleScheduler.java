package com.goti.utils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.goti.dto.response.GameInfo;
import com.goti.service.ResaleListingService;
import com.goti.service.ResalePriceService;
import com.goti.service.TicketService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResaleScheduler {

	private final TicketService ticketService;
	private final ResaleListingService listingService;
	private final ResalePriceService priceService;

	@Scheduled(cron = "0 0 14,15,18,19 * * *")
	@Scheduled(cron = "0 30 19 * * *")
	public void autoCancelExpiredListings() {
		LocalDateTime threshold = LocalDateTime.now().minusHours(1);

		List<UUID> expiredGameIds = ticketService.getExpiredGameIds(threshold);

		// TODO : 아래 for문을 batch 나 event로 변경할 것
		for (UUID gameId : expiredGameIds) {
			try {
				listingService.cancelListingCauseGameStart(gameId);
			} catch (Exception e) {
				log.error("리셀 등록 취소 실패 - gameId: {}",
					gameId, e);
			}
		}
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void updateDailyBasePrices() {
		List<GameInfo> upcomingGames = ticketService.getUpcomingGames();
		for (GameInfo info : upcomingGames) {
			try {
				priceService.updateDailyBasePrice(info.gameId(), info.gradeId());
			} catch (Exception e) {
				log.error("기준가 업데이트 실패 - gameId: {}, gradeId: {}",
					info.gameId(), info.gradeId(), e);
			}
		}
	}
}

