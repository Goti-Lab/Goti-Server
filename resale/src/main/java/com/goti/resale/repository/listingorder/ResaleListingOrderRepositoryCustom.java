package com.goti.resale.repository.listingorder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.goti.resale.constants.ResaleListingOrderStatus;
import com.goti.resale.domain.entity.resale.ResaleListingOrderEntity;

public interface ResaleListingOrderRepositoryCustom {
	Page<ResaleListingOrderEntity> getSalesHistory(
		UUID sellerId,
		List<ResaleListingOrderStatus> statuses,
		Integer months,
		LocalDate startDate,
		LocalDate endDate,
		Pageable pageable
	);
}