package com.goti.resale.repository.hold;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.resale.constants.ResaleHoldStatus;
import com.goti.resale.domain.entity.resale.ResaleHoldEntity;

public interface ResaleHoldRepository extends JpaRepository<ResaleHoldEntity, UUID>, ResaleHoldRepositoryCustom {

	Optional<ResaleHoldEntity> findByIdAndUserIdAndStatus(UUID id, UUID userId, ResaleHoldStatus status);
	
}

