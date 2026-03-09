package com.goti.repository.listing;

import java.util.List;
import java.util.UUID;

import com.goti.constants.ResaleListingStatus;
import com.goti.domain.entity.resale.ResaleListingEntity;

public interface ResaleListingRepositoryCustom {
	boolean existsByTicketIdAndListingStatusIn(UUID ticketId, List<ResaleListingStatus> statuses);

	List<ResaleListingEntity> findByGameAndListingStatusIn(UUID gameId, List<ResaleListingStatus> statuses);

	List<ResaleListingEntity> findByGameAndGradeAndListingStatus(
		UUID gameId,
		UUID gradeId,
		ResaleListingStatus listingStatus
	);
}
