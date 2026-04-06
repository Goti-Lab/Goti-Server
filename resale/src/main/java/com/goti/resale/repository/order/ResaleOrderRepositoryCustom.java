package com.goti.resale.repository.order;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.goti.resale.domain.entity.resale.ResaleOrderEntity;

public interface ResaleOrderRepositoryCustom {
	List<ResaleOrderEntity> findCompletedPurchaseOrders(
		UUID buyerId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	);
}
