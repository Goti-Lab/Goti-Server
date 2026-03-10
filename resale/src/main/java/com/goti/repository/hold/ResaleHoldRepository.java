package com.goti.repository.hold;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.constants.ResaleHoldStatus;
import com.goti.domain.entity.resale.ResaleHoldEntity;

public interface ResaleHoldRepository extends JpaRepository<ResaleHoldEntity, UUID>, ResaleHoldRepositoryCustom {

	Optional<ResaleHoldEntity> findByIdAndUserIdAndStatus(UUID id, UUID userId, ResaleHoldStatus status);
	
}

