package com.goti.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.constants.ResaleListingStatus;
import com.goti.domain.entity.resale.ResaleListingEntity;

public interface ResaleListingRepository extends JpaRepository<ResaleListingEntity, UUID> {

	boolean existsByTicketIdAndListingStatusIn(UUID ticketId, List<ResaleListingStatus> statuses);

	List<ResaleListingEntity> findByGameIdAndListingStatusIn(UUID GameId, List<ResaleListingStatus> statuses);

	List<ResaleListingEntity> findBySellerId(UUID sellerId);
}