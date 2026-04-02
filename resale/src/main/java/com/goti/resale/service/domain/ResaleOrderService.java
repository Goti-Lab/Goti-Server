package com.goti.resale.service.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.goti.domain.vo.TransactionItemVO;
import com.goti.resale.domain.entity.resale.ResaleHoldEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.dto.response.ResaleOrderCreateResponse;
import com.goti.resale.dto.response.ResalePurchaseListResponse;

public interface ResaleOrderService {
	String generateOrderNumber();

	String generateResaleTicketNumber(String resaleSuffix, String num);

	List<TransactionItemVO> calculateOrderItems(UUID buyerId, List<ResaleHoldEntity> holds,
		ResaleRestrictionEntity restriction);

	void validatePossessionLimit(int currentOwnedCount, int pendingCount, int requestCount);

	ResaleOrderCreateResponse initOrder(UUID buyerId, List<ResaleHoldEntity> holds, UUID gameId);

	List<ResalePurchaseListResponse> getPurchasesByMember(
		UUID buyerId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	);
}
