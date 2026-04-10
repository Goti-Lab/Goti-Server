package com.goti.payment.service.application;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.goti.payment.dto.request.enums.PurchaseSearchType;

import com.goti.payment.dto.response.SeatGradeInfoResponse;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;
import com.goti.payment.dto.response.PurchaseSearchResponse;
import com.goti.payment.infra.ResaleOrderClient;
import com.goti.payment.infra.TicketingOrderClient;

import lombok.RequiredArgsConstructor;

import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PurchaseSearchService {
	private static final String NORMAL = "NORMAL";
	private static final String RESALE = "RESALE";

	private final TicketingOrderClient ticketingOrderClient;
	private final ResaleOrderClient resaleOrderClient;

	@Transactional(readOnly = true)
	public Page<PurchaseSearchResponse> getAll(
		UUID memberId,
		PurchaseSearchType type,
		String keyword,
		Integer months,
		LocalDate startDate,
		LocalDate endDate,
		Pageable pageable
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

		List<PurchaseSearchResponse> combinedPurchases = Stream.concat(normalOrders.stream(), resaleOrders.stream())
			.filter(order -> matchesKeyword(order, keyword))
			.sorted(Comparator.comparing(PurchaseSearchResponse::orderedAt).reversed())
			.toList();

		return toPage(combinedPurchases, pageable);
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
				order.gameTitle(),
				order.gameDate(),
				extractSeatInfos(order.seatGradeGroups())
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

	private List<String> extractSeatInfos(List<SeatGradeInfoResponse> seatGradeGroups) {
		if (seatGradeGroups == null) {
			return List.of();
		}

		return seatGradeGroups.stream()
			.flatMap(group -> group.seatInfos().stream()
				.map(seatInfo -> combineSeatGradeAndSeatInfo(group.seatGradeName(), seatInfo)))
			.toList();
	}

	private String combineSeatGradeAndSeatInfo(String seatGradeName, String seatInfo) {
		if (!StringUtils.hasText(seatGradeName)) {
			return seatInfo;
		}

		return seatGradeName + " " + seatInfo;
	}

	private Page<PurchaseSearchResponse> toPage(List<PurchaseSearchResponse> combinedPurchases, Pageable pageable) {
		int start = (int) pageable.getOffset();
		if (start >= combinedPurchases.size()) {
			return new PageImpl<>(List.of(), pageable, combinedPurchases.size());
		}

		int end = Math.min(start + pageable.getPageSize(), combinedPurchases.size());
		return new PageImpl<>(combinedPurchases.subList(start, end), pageable, combinedPurchases.size());
	}

	private boolean matchesKeyword(PurchaseSearchResponse order, String keyword) {
		if (!StringUtils.hasText(keyword)) {
			return true;
		}

		String normalizedKeyword = keyword.trim().toLowerCase();

		return contains(order.orderNumber(), normalizedKeyword)
			|| contains(order.gameTitle(), normalizedKeyword)
			|| order.seatInfos().stream().anyMatch(seatInfo -> contains(seatInfo, normalizedKeyword));
	}

	private boolean contains(String value, String keyword) {
		return value != null && value.toLowerCase().contains(keyword);
	}
}
