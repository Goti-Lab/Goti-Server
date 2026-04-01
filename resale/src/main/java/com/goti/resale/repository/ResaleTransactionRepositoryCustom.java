package com.goti.resale.repository;

import java.util.List;
import java.util.UUID;

import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;

public interface ResaleTransactionRepositoryCustom {
	List<ResaleTransactionEntity> findListings(List<UUID> orderIds);
}
