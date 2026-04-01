package com.goti.resale.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goti.resale.constants.ResaleListingOrderStatus;
import com.goti.resale.domain.entity.resale.ResaleListingOrderEntity;

public interface ResaleListingOrderRepository extends JpaRepository<ResaleListingOrderEntity, UUID> {
	@Query("SELECT r FROM ResaleListingOrderEntity r "
		+ "WHERE r.sellerId = :sellerId "
		+ "AND r.gradeId = :gradeId "
		+ "AND r.orderStatus IN :statuses")
	Optional<ResaleListingOrderEntity> findBySellerAndGradeWithStatus(
		@Param("sellerId") UUID sellerId,
		@Param("gradeId") UUID gradeId,
		@Param("statuses") List<ResaleListingOrderStatus> statuses
	);
}