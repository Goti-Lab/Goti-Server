package com.goti.repository.history;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.goti.domain.entity.resale.ResalePriceHistoryEntity;

public interface ResalePriceHistoryRepository
	extends JpaRepository<ResalePriceHistoryEntity, UUID>, ResalePriceHistoryRepositoryCustom {
}
