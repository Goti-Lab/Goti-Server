package com.goti.payment.service.application;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;
import com.goti.payment.dto.request.enums.PurchaseSearchType;
import com.goti.payment.dto.response.PurchaseSearchResponse;
import com.goti.payment.infra.ResaleOrderClient;
import com.goti.payment.infra.TicketingOrderClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PurchaseSearchService {
	private static final String NORMAL = "NORMAL";
	private static final String RESALE = "RESALE";

	private final TicketingOrderClient ticketingOrderClient;
	private final ResaleOrderClient resaleOrderClient;

	@Transactional(readOnly = true)
	public List<PurchaseSearchResponse> getAll(
		UUID memberId,
		PurchaseSearchType type,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		Preconditions.validate(
			memberId != null,
			ErrorCode.AUTH_INVALID
		);

		PurchaseSearchType purchaseSearchType = type != null ? type : PurchaseSearchType.ALL;

		List<PurchaseSearchResponse> normalOrders = getNormalOrders(
			memberId,
			purchaseSearchType,
			months,
			startDate,
			endDate
		);

		List<PurchaseSearchResponse> resaleOrders = getResaleOrders(
			memberId,
			purchaseSearchType,
			months,
			startDate,
			endDate
		);

		return Stream.concat(normalOrders.stream(), resaleOrders.stream())
			.sorted(Comparator.comparing(PurchaseSearchResponse::orderedAt).reversed())
			.toList();
	}

	private List<PurchaseSearchResponse> getNormalOrders(
		UUID memberId,
		PurchaseSearchType type,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		if (type == PurchaseSearchType.RESALE) {
			return List.of();
		}

		return ticketingOrderClient.getOrders(memberId, months, startDate, endDate).stream()
			.map(order -> new PurchaseSearchResponse(
				NORMAL,
				order.orderId(),
				order.orderNumber(),
				order.orderStatus().name(),
				order.totalQuantity(),
				order.totalAmount(),
				order.orderedAt(),
				order.gameId(),
				order.stadiumId(),
				null,
				null,
				List.of()
			))
			.toList();
	}

	private List<PurchaseSearchResponse> getResaleOrders(
		UUID memberId,
		PurchaseSearchType type,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		if (type == PurchaseSearchType.NORMAL) {
			return List.of();
		}

		return resaleOrderClient.getPurchases(memberId, months, startDate, endDate).stream()
			.map(order -> new PurchaseSearchResponse(
				RESALE,
				order.orderId(),
				order.orderNumber(),
				order.orderStatus(),
				order.totalQuantity(),
				order.totalAmount(),
				order.orderedAt(),
				order.gameId(),
				null,
				order.gameTitle(),
				order.gameDate(),
				order.seatInfos()
			))
			.toList();
	}
}
