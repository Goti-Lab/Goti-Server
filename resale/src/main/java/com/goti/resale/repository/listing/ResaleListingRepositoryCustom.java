package com.goti.resale.repository.listing;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;

public interface ResaleListingRepositoryCustom {
	Page<ResaleListingEntity> findMySales(
		UUID sellerId,
		List<ResaleListingStatus> statuses,
		Integer months,
		LocalDate startDate,
		LocalDate endDate,
		Pageable pageable
	);
}
