package com.goti.resale.repository.hold;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.goti.resale.constants.ResaleHoldStatus;
import com.goti.resale.domain.entity.resale.ResaleHoldEntity;

public interface ResaleHoldRepositoryCustom {
	List<ResaleHoldEntity> findExpiredResaleHolds(
		@Param("status") ResaleHoldStatus status,
		@Param("now") LocalDateTime now,
		Pageable pageable
	);
}
