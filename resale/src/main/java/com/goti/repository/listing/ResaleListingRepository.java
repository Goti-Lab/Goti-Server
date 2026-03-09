package com.goti.repository.listing;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.domain.entity.resale.ResaleListingEntity;

public interface ResaleListingRepository extends JpaRepository<ResaleListingEntity, UUID>, ResaleListingRepositoryCustom {

}
