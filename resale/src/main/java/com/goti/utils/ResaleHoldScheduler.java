package com.goti.utils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.goti.config.properties.ResaleHoldExpiryProperties;
import com.goti.constants.ResaleHoldStatus;
import com.goti.constants.ResaleListingStatus;
import com.goti.domain.entity.resale.ResaleHoldEntity;
import com.goti.repository.ResaleHoldRepository;
import com.goti.repository.listing.ResaleListingRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
	prefix = "seat.hold-expiry",
	name = "enabled",
	havingValue = "true",
	matchIfMissing = true
)
public class ResaleHoldScheduler {

	private final ResaleListingRepository resaleListingRepository;
	private final ResaleHoldRepository resaleHoldRepository;
	private final ResaleHoldExpiryProperties resaleHoldExpiryProperties;

	@Scheduled(fixedDelayString = "${seat.hold-expiry.fixed-delay-ms}")
	@Transactional
	public void expireResaleHolds() {
		LocalDateTime now = LocalDateTime.now();
		Pageable pageable = PageRequest.of(0, resaleHoldExpiryProperties.batchSize());

		List<ResaleHoldEntity> expiredHolds = resaleHoldRepository
			.findExpiredResaleHolds(
				ResaleHoldStatus.HOLDING,
				now,
				pageable
			);
		if (expiredHolds.isEmpty()) {
			return;
		}
		List<UUID> holdIds = expiredHolds.stream()
			.map(ResaleHoldEntity::getId)
			.toList();

		List<UUID> listingIds = expiredHolds.stream()
			.map(hold -> hold.getResaleListing().getId())
			.toList();

		resaleListingRepository.updateListingStatusByBatch(
			listingIds,
			ResaleListingStatus.HOLD,
			ResaleListingStatus.LISTING
		);

		resaleHoldRepository.updateStatusToReleased(
			holdIds,
			ResaleHoldStatus.RELEASED,
			now
		);
	}
}
