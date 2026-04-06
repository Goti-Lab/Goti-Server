package com.goti.resale.service.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.resale.domain.entity.resale.ResaleHoldEntity;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;

public interface ResaleHoldExpiryService {
	void expire(
		ResaleListingEntity resaleListing,
		ResaleHoldEntity resaleHold,
		LocalDateTime now
	);

	void expire(UUID holdId, LocalDateTime now);
}