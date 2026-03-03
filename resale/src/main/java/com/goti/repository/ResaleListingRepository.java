package com.goti.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.goti.constants.ResaleListingStatus;
import com.goti.domain.entity.resale.ResaleListingEntity;

public interface ResaleListingRepository extends JpaRepository<ResaleListingEntity, UUID> {

	Optional<ResaleListingEntity> findByTicketId(UUID ticketId);

	boolean existsByTicketId(UUID ticketId);

	boolean existsByTicketIdAndListingStatus(UUID ticketId, ResaleListingStatus status);

	@Query("SELECT COUNT(r) FROM ResaleListingEntity r " +
		"WHERE r.sellerId = :sellerId " +
		"AND r.gameId = :gameId " +
		"AND r.listedAt >= :since")
	int countSellerGameListings(
		@Param("sellerId") UUID sellerId,
		@Param("gameId") UUID gameId,
		@Param("since") LocalDateTime since
	);

	@Query("SELECT COUNT(r) FROM ResaleListingEntity r " +
		"WHERE r.sellerId = :sellerId " +
		"AND r.gameId = :gameId " +
		"AND r.listingStatus = 'CANCELED' " +
		"AND r.canceledAt >= :since")
	int countSellerGameCancellations(
		@Param("sellerId") UUID sellerId,
		@Param("gameId") UUID gameId,
		@Param("since") LocalDateTime since
	);

	@Query("SELECT r FROM ResaleListingEntity r " +
		"WHERE r.listingStatus = 'FROZEN' " +
		"AND r.defrostAt <= :now")
	List<ResaleListingEntity> findDefrostableListings(@Param("now") LocalDateTime now);

	@Query("SELECT r FROM ResaleListingEntity r " +
		"WHERE r.gameId = :gameId " +
		"AND r.listingStatus IN ('RESELL_AVAILABLE', 'FROZEN')")
	List<ResaleListingEntity> findExpirableListingsByGame(@Param("gameId") UUID gameId);
}