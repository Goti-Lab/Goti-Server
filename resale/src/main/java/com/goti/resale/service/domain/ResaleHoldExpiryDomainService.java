package com.goti.resale.service.domain;

import java.time.LocalDateTime;

import com.goti.resale.domain.entity.resale.ResaleHoldEntity;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;

public interface ResaleHoldExpiryDomainService {
	void expire(
		ResaleListingEntity resaleListing,
		ResaleHoldEntity resaleHold,
		LocalDateTime now
	);
}