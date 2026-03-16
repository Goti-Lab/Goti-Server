package com.goti.resale.service.application;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.resale.constants.ResaleGraphRange;
import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResalePriceHistoryEntity;
import com.goti.resale.dto.response.ResalePriceHistoryResponse;
import com.goti.resale.repository.history.ResalePriceHistoryRepository;
import com.goti.resale.repository.listing.ResaleListingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResalePriceService {
	private final ResalePriceHistoryRepository priceHistoryRepository;
	private final ResaleListingRepository listingRepository;

	private static final ZoneId ZONE_ID = ZoneId.of("Asia/Seoul");

	@Transactional
	public void updateDailyBasePrice(UUID gameId, UUID gradeId) {
		LocalDate yesterday = LocalDate.now().minusDays(1);

		var startOfDay = yesterday.atStartOfDay(ZONE_ID).toInstant();
		var endOfDay = yesterday.atTime(LocalTime.MAX).atZone(ZONE_ID).toInstant();

		Optional<ResalePriceHistoryEntity> resaleHistories = priceHistoryRepository
			.findByGameAndGradeAndDate(
				gameId, gradeId, startOfDay, endOfDay
			);

		if (resaleHistories.isEmpty()) {
			log.info("거래내역이 없습니다., gameId : {}, gradeId: {}", gameId, gradeId);
			return;
		}

		Integer lastPrice = resaleHistories.get().getTransactionPrice();

		List<ResaleListingEntity> resaleListings = listingRepository.findByGameAndGradeAndStatus(
			gameId,
			gradeId,
			ResaleListingStatus.LISTING
		);

		for (ResaleListingEntity resaleListing : resaleListings) {
			resaleListing.updateDailyBasePrice(lastPrice);
		}
		listingRepository.saveAll(resaleListings);
	}

	@Transactional(readOnly = true)
	public List<ResalePriceHistoryResponse> getHistory(UUID gameId, UUID gradeId, ResaleGraphRange range) {
		Instant since = range.getTime();

		List<ResalePriceHistoryEntity> resaleHistories = priceHistoryRepository.findByGameAndGrade(
			gameId,
			gradeId,
			since
		);

		return resaleHistories
			.stream()
			.map(ResalePriceHistoryResponse::from)
			.toList();
	}
}
