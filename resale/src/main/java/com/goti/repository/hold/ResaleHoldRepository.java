package com.goti.repository.hold;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.constants.ResaleHoldStatus;
import com.goti.domain.entity.resale.ResaleHoldEntity;

public interface ResaleHoldRepository extends JpaRepository<ResaleHoldEntity, UUID>, ResaleHoldRepositoryCustom {

	List<ResaleHoldEntity> findAllByIdInAndUserIdAndStatus(List<UUID> ids, UUID userId, ResaleHoldStatus status);

}

