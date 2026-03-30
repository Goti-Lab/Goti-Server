package com.goti.resale.repository.listing;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;

public interface ResaleListingRepository extends JpaRepository<ResaleListingEntity, UUID>, ResaleListingRepositoryCustom {

	List<ResaleListingEntity> findAllBySellerId(UUID sellerId);

	Long countByGameIdAndGradeIdAndListingStatus(UUID gameId, UUID gradeId, ResaleListingStatus status);

	Long countByGameIdAndListingStatus(UUID gameId, ResaleListingStatus status);

	boolean existsByTicketIdAndListingStatusIn(UUID ticketId, List<ResaleListingStatus> statuses);

	List<ResaleListingEntity> findByGameIdInAndListingStatusIn(List<UUID> gameId, List<ResaleListingStatus> statuses);

	List<ResaleListingEntity> findAllByListingOrderId(UUID listingOrderId);

	@Query("SELECT r FROM ResaleListingEntity r "
		+ "WHERE r.gameId = :gameId "
		+ "AND r.gradeId = :gradeId "
		+ "AND r.listingStatus = :listingStatus")
	List<ResaleListingEntity> findByGameAndGradeAndStatus(
		@Param("gameId") UUID gameId,
		@Param("gradeId") UUID gradeId,
		@Param("listingStatus") ResaleListingStatus listingStatus
	);

	Long countBySellerIdAndListingStatusIn(UUID sellerId, List<ResaleListingStatus> listingStatus);

}
