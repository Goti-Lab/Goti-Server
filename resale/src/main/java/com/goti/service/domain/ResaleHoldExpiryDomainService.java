package com.goti.service.domain;

import java.time.LocalDateTime;

import com.goti.domain.entity.resale.ResaleHoldEntity;
import com.goti.domain.entity.resale.ResaleListingEntity;

public interface ResaleHoldExpiryDomainService {
	void expire(
		ResaleListingEntity resaleListing,
		ResaleHoldEntity resaleHold,
		LocalDateTime now
	);
}