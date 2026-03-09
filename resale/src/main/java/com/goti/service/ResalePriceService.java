package com.goti.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.ResaleListingStatus;
import com.goti.domain.entity.resale.ResaleListingEntity;
import com.goti.domain.entity.resale.ResalePriceHistoryEntity;
import com.goti.repository.ResaleListingRepository;
import com.goti.repository.ResalePriceHistoryRepository;

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

		List<ResalePriceHistoryEntity> resaleHistories = priceHistoryRepository
			.findByGameIdAndGradeIdAndTransactionDateOrderByTransactionPriceAsc(
				gameId, gradeId, yesterday
			);

		if (resaleHistories.isEmpty()) {
			log.info("거래내역이 없습니다., gameId : {}, gradeId: {}", gameId, gradeId);
			return;
		}

		List<Integer> prices = resaleHistories.stream()
			.map(ResalePriceHistoryEntity::getTransactionPrice)
			.toList();

		Integer medianPrice = calculateMedian(prices);

		List<ResaleListingEntity> resaleListings = listingRepository.findByGameIdAndGradeIdAndListingStatus(
			gameId,
			gradeId,
			ResaleListingStatus.LISTING
		);

		for (ResaleListingEntity resaleListing : resaleListings) {
			resaleListing.updateDailyBasePrice(medianPrice);
		}
		listingRepository.saveAll(resaleListings);
	}

	private Integer calculateMedian(List<Integer> sortedPrices) {
		int size = sortedPrices.size();
		if (size % 2 == 0) {
			return (sortedPrices.get(size / 2 - 1) + sortedPrices.get(size / 2)) / 2;
		} else {
			return sortedPrices.get(size / 2);
		}
	}
}
