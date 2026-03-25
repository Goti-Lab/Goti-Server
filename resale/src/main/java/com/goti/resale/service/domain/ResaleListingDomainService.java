package com.goti.resale.service.domain;

import java.util.UUID;

import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.dto.response.ResaleTicketResponse;

public interface ResaleListingDomainService {
	void validateListingCreation(
		ResaleTicketResponse ticketInfo,
		UUID sellerId,
		Integer listingPrice,
		ResaleRestrictionEntity restriction
	);

	void validateListingCancellation(
		UUID sellerId,
		ResaleListingEntity resaleListing,
		ResaleRestrictionEntity resaleRestriction
	);
}
