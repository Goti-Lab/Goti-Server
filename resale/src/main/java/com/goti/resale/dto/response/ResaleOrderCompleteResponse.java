package com.goti.resale.dto.response;

import java.util.List;
import java.util.UUID;

import com.goti.resale.constants.ResaleOrderStatus;
import com.goti.resale.domain.entity.resale.ResaleOrderEntity;
import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;

public record ResaleOrderCompleteResponse(
	// TODO: 페이지가 나오면 필드 조정
	UUID orderId,
	String orderNumber,
	UUID buyerId,
	Integer totalAmount,
	ResaleOrderStatus orderStatus,
	List<TransactionItemSummary> items
) {
	public record TransactionItemSummary(
		UUID transactionId,
		UUID listingId,
		String seatInfo,
		Integer price
	) {
		public static TransactionItemSummary from(ResaleTransactionEntity entity) {
			return new TransactionItemSummary(
				entity.getId(),
				entity.getListing().getId(),
				entity.getListing().getSeatInfo(),
				entity.getTransactionPrice()
			);
		}
	}

	public static ResaleOrderCompleteResponse from(ResaleOrderEntity order, List<ResaleTransactionEntity> transactions) {
		List<TransactionItemSummary> itemSummaries = transactions.stream()
			.map(TransactionItemSummary::from)
			.toList();

		return new ResaleOrderCompleteResponse(
			order.getId(),
			order.getOrderNumber(),
			order.getBuyerId(),
			order.getTotalAmount(),
			order.getOrderStatus(),
			itemSummaries
		);
	}
}
