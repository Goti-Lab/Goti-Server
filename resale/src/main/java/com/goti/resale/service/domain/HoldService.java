package com.goti.resale.service.domain;

import java.util.UUID;

import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.dto.request.ResaleHoldRequest;
import com.goti.resale.dto.response.ResaleHoldResponse;
import com.goti.resale.dto.response.ResaleReleaseResponse;

public interface HoldService {
	void validateHoldable(ResaleListingEntity listing, UUID buyerId);

	ResaleHoldResponse hold(UUID buyerId, ResaleHoldRequest request);

	ResaleReleaseResponse release(UUID holdId);
}
