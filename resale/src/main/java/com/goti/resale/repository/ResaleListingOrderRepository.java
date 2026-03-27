package com.goti.resale.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.resale.constants.ResaleListingOrderStatus;
import com.goti.resale.domain.entity.resale.ResaleListingOrderEntity;

public interface ResaleListingOrderRepository extends JpaRepository<ResaleListingOrderEntity, UUID> {
	Optional<ResaleListingOrderEntity> findBySellerIdAndSectionIdAndOrderStatusIn(
		UUID sellerId,
		UUID sectionId,
		List<ResaleListingOrderStatus> statuses
	);
}
