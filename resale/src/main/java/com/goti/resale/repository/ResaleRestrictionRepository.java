package com.goti.resale.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;

public interface ResaleRestrictionRepository extends JpaRepository<ResaleRestrictionEntity, UUID> {

	Optional<ResaleRestrictionEntity> findByUserId(UUID userId);

	List<ResaleRestrictionEntity> findByUserIdIn(List<UUID> userIds);
}