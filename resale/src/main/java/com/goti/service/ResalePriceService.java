package com.goti.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.ResaleListingStatus;
import com.goti.domain.entity.resale.ResaleListingEntity;
import com.goti.domain.entity.resale.ResalePriceHistoryEntity;
import com.goti.repository.history.ResalePriceHistoryRepository;
import com.goti.repository.listing.ResaleListingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResalePriceService {
	private final ResalePriceHistoryRepository priceHistoryRepository;
	private final ResaleListingRepository listingRepository;

	@Transactional
	public void updateDailyBasePrice(UUID gameId, UUID gradeId) {
		LocalDate yesterday = LocalDate.now().minusDays(1);

		Optional<ResalePriceHistoryEntity> resaleHistories = priceHistoryRepository
			.findByGameAndGradeAndDate(
				gameId, gradeId, yesterday
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

}
