package com.goti.repository.listing;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goti.constants.ResaleListingStatus;
import com.goti.domain.entity.resale.ResaleListingEntity;

public interface ResaleListingRepository extends JpaRepository<ResaleListingEntity, UUID> {

	List<ResaleListingEntity> findAllBySellerId(UUID sellerId);

	boolean existsByTicketIdAndListingStatusIn(UUID ticketId, List<ResaleListingStatus> statuses);

	List<ResaleListingEntity> findByGameIdAndListingStatusIn(UUID gameId, List<ResaleListingStatus> statuses);

	@Query("SELECT r FROM ResaleListingEntity r "
		+ "WHERE r.gameId = :gameId "
		+ "AND r.gradeId = :gradeId "
		+ "AND r.listingStatus = :listingStatus")
	List<ResaleListingEntity> findByGameAndGradeAndStatus(
		@Param("gameId") UUID gameId,
		@Param("gradeId") UUID gradeId,
		@Param("listingStatus") ResaleListingStatus listingStatus
	);

	@Modifying(clearAutomatically = true)
	@Query("UPDATE ResaleListingEntity r SET r.listingStatus = :newStatus " +
		"WHERE r.id IN :listingIds AND r.listingStatus = :oldStatus")
	void updateListingStatusByBatch(
		@Param("listingIds") List<UUID> listingIds,
		@Param("oldStatus") ResaleListingStatus oldStatus,
		@Param("newStatus") ResaleListingStatus newStatus
	);
}
