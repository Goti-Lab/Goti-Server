package com.goti.repository.hold;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import com.goti.constants.ResaleHoldStatus;
import com.goti.domain.entity.resale.ResaleHoldEntity;

public interface ResaleHoldRepositoryCustom {
	List<ResaleHoldEntity> findExpiredResaleHolds(
		@Param("status") ResaleHoldStatus status,
		@Param("now") LocalDateTime now,
		Pageable pageable
	);
}
