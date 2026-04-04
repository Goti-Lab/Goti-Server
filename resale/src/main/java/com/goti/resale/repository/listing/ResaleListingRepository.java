package com.goti.resale.repository.listing;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;

public interface ResaleListingRepository extends JpaRepository<ResaleListingEntity, UUID> {
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

	@Query("SELECT r.listingStatus, COUNT(r) FROM ResaleListingEntity r "
		+ "WHERE r.sellerId = :sellerId "
		+ "GROUP BY r.listingStatus")
	List<Object[]> countResale(
		@Param("sellerId") UUID sellerId
	);

}
