package com.goti.resale.service.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.goti.resale.constants.ResaleListingOrderStatus;
import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.constants.ResaleStatus;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleListingOrderEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.dto.request.ResaleListingCancelRequest;
import com.goti.resale.dto.request.ResaleListingOrderCreateRequest;
import com.goti.resale.dto.response.ResaleListingOrderCreateResponse;
import com.goti.resale.dto.response.ResaleListingResponse;
import com.goti.resale.dto.response.ResaleListingOrderResponse;
import com.goti.resale.dto.response.ResaleListingsCountResponse;
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

	Page<ResaleListingOrderResponse> getSalesHistory(
		UUID sellerId,
		List<ResaleListingOrderStatus> statuses,
		Integer months,
		LocalDate startDate,
		LocalDate endDate,
		Pageable pageable
	);

	ResaleListingEntity getListing(UUID sellerId, UUID listingId);

	ResaleListingsCountResponse getResaleCount(UUID sellerId);

	List<ResaleListingEntity> getListingsByOrderId(UUID orderId);

	ResaleStatus getResaleStatus(UUID gameId);

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

	void updateListingOrders(Set<ResaleListingOrderEntity> listingOrders);
}
