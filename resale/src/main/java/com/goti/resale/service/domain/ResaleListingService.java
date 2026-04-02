package com.goti.resale.service.domain;

import java.util.UUID;

import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.dto.request.ResaleListingCancelRequest;
import com.goti.resale.dto.request.ResaleListingOrderCreateRequest;
import com.goti.resale.dto.response.ResaleListingOrderCreateResponse;
import com.goti.resale.dto.response.ResaleListingResponse;
import com.goti.resale.dto.response.ResaleTicketResponse;

public interface ResaleListingService {
	String generateListingOrderNumber();

	ResaleListingOrderCreateResponse createListingOrder(
		UUID sellerId,
		ResaleListingOrderCreateRequest request
	);

	ResaleListingResponse cancelListing(
		UUID sellerId,
		ResaleListingCancelRequest request
	);

	void cancelListingOrder(
		UUID sellerId,
		UUID orderId
	);

	Long countListings(UUID sellerId);

	Long countSold(UUID sellerId);

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
