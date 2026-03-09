package com.goti.repository.listing;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.domain.entity.resale.ResaleListingEntity;

public interface ResaleListingRepository
	extends JpaRepository<ResaleListingEntity, UUID>, ResaleListingRepositoryCustom {

	List<ResaleListingEntity> findBySellerId(UUID sellerId);

}
