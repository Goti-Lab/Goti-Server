package com.goti.resale.service.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.goti.resale.constants.ResaleListingStatus;
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

	Page<ResaleListingEntity> getMySales(
		UUID sellerId,
		List<ResaleListingStatus> statuses,
		Integer months,
		LocalDate startDate,
		LocalDate endDate,
		Pageable pageable
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
