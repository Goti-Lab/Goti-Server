package com.goti.resale.service.domain;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.f4b6a3.tsid.TsidCreator;
import com.goti.constants.messages.ErrorCode;
import com.goti.domain.vo.TransactionItemVO;
import com.goti.global.validation.Preconditions;
import com.goti.resale.constants.ResaleTransactionStatus;
import com.goti.resale.domain.entity.resale.ResaleHoldEntity;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleOrderEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;
import com.goti.resale.dto.request.ResaleTransactionItemRequest;
import com.goti.resale.dto.response.ResaleOrderCreateResponse;
import com.goti.resale.dto.response.ResalePurchaseListResponse;
import com.goti.resale.infra.TicketClient;
import com.goti.resale.infra.dto.ResaleOrderCreatedEvent;
import com.goti.resale.repository.ResaleOrderRepository;
import com.goti.resale.repository.ResaleTransactionRepository;
import com.goti.resale.utils.ResalePricePolicy;
import com.goti.resale.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleOrderServiceImpl implements ResaleOrderService {
	private static final DateTimeFormatter ORDER_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
	private static final DateTimeFormatter TICKET_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("MMdd");
	private static final List<Integer> ALLOWED_MONTHS = List.of(1, 3, 6);

	private final ResaleOrderRepository resaleOrderRepository;
	private final ResaleTransactionRepository resaleTransactionRepository;
	private final ResaleRestrictionService restrictionDomainService;
	private final ResaleRestrictionHandler resaleRestrictionHandler;
	private final ResalePricePolicy resalePricePolicy;
	private final TicketClient ticketClient;
	private final ApplicationEventPublisher eventPublisher;

	@Override
	public String generateOrderNumber() {
		String tsidSuffix = TsidCreator.getTsid().toString();
		return "ORD" + "-" +
			LocalDate.now().format(ORDER_NUMBER_FORMATTER) +
			tsidSuffix.substring(tsidSuffix.length() - 6);
	}

	@Override
	public String generateResaleTicketNumber(String resaleSuffix, String num) {
		return "RST" + "-" +
			LocalDate.now().format(TICKET_NUMBER_FORMATTER) +
			resaleSuffix +
			"-" + num;
	}

	@Override
	public List<TransactionItemVO> calculateOrderItems(UUID buyerId, List<ResaleHoldEntity> holds,
		ResaleRestrictionEntity restriction) {
		List<TransactionItemVO> itemVOs = new ArrayList<>();
		for (ResaleHoldEntity hold : holds) {
			ResaleListingEntity listing = hold.getResaleListing();

			resaleRestrictionHandler.validateCanBuy(restriction, listing.getGameId());

			ResalePricePolicy.FeeResult feeResult = resalePricePolicy.validateTransactionFee(
				listing.getListingPrice()
			);
			itemVOs.add(new TransactionItemVO(listing, feeResult));
		}
		return itemVOs;
	}

	@Override
	public void validatePossessionLimit(int currentOwnedCount, int pendingCount, int requestCount) {
		resaleRestrictionHandler.validatePossessionLimit(currentOwnedCount, pendingCount, requestCount);
	}

	@Override
	@Transactional
	public ResaleOrderCreateResponse initOrder(UUID buyerId, List<ResaleHoldEntity> holds, UUID gameId) {
		int ownedCount = ticketClient.getOwnedTicketCount(buyerId, gameId);
		int pendingCount = resaleTransactionRepository.countTransactions(
			buyerId, gameId, ResaleTransactionStatus.PENDING);

		validatePossessionLimit(ownedCount, pendingCount, holds.size());

		ResaleRestrictionEntity restriction = restrictionDomainService.getOrCreateRestriction(buyerId);
		List<TransactionItemVO> itemVOs = calculateOrderItems(buyerId, holds, restriction);

		int totalBuyerAmount = itemVOs.stream()
			.mapToInt(TransactionItemVO::getBuyerTotal)
			.sum();
		int totalBuyerFee = itemVOs.stream()
			.mapToInt(TransactionItemVO::getBuyerFee)
			.sum();
		int totalSellerFee = itemVOs.stream()
			.mapToInt(TransactionItemVO::getSellerFee)
			.sum();

		ResaleOrderEntity resaleOrder = createOrder(buyerId, totalBuyerAmount);

		List<ResaleTransactionEntity> transactions = createTransactions(resaleOrder, buyerId, itemVOs);

		List<ResaleTransactionItemRequest> paymentItems = transactions.stream()
			.map(t -> new ResaleTransactionItemRequest(
				t.getId(),
				t.getSellerId(),
				t.getSellerTotal()
			)).toList();

		eventPublisher.publishEvent(new ResaleOrderCreatedEvent(
			resaleOrder.getId(),
			buyerId,
			totalBuyerAmount,
			totalBuyerFee,
			totalSellerFee,
			paymentItems,
			String.valueOf(resaleOrder.getId())
		));

		return ResaleOrderCreateResponse.from(
			resaleOrder,
			itemVOs.size()
		);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ResalePurchaseListResponse> getPurchasesByMember(
		UUID buyerId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		Preconditions.validate(
			buyerId != null,
			ErrorCode.AUTH_INVALID
		);
		validatePeriodFilter(months, startDate, endDate);

		List<ResaleOrderEntity> orders = resaleOrderRepository.findCompletedPurchaseOrders(
			buyerId,
			months,
			startDate,
			endDate
		);

		if (orders.isEmpty()) {
			return List.of();
		}

		List<UUID> orderIds = orders.stream()
			.map(ResaleOrderEntity::getId)
			.toList();

		List<ResaleTransactionEntity> transactions = resaleTransactionRepository.findListings(orderIds);
		Map<UUID, List<ResaleTransactionEntity>> transactionsByOrderId = transactions.stream()
			.collect(Collectors.groupingBy(
				transaction -> transaction.getResaleOrder().getId(),
				LinkedHashMap::new,
				Collectors.toList()
			));

		return orders.stream()
			.map(order -> toPurchaseListResponse(order, transactionsByOrderId.getOrDefault(order.getId(), List.of())))
			.toList();
	}

	private ResaleOrderEntity createOrder(UUID buyerId, int totalAmount) {
		ResaleOrderEntity resaleOrder = ResaleOrderEntity.create(
			generateOrderNumber(),
			buyerId,
			totalAmount
		);
		return resaleOrderRepository.save(resaleOrder);
	}

	private List<ResaleTransactionEntity> createTransactions(
		ResaleOrderEntity order,
		UUID buyerId,
		List<TransactionItemVO> itemVOs
	) {
		String tsidSuffix = TsidCreator.getTsid().toString();
		String resaleSuffix = tsidSuffix.substring(tsidSuffix.length() - 6);

		List<ResaleTransactionEntity> transactions = new ArrayList<>();
		for (int i = 0; i < itemVOs.size(); i++) {
			TransactionItemVO item = itemVOs.get(i);
			ResaleTransactionEntity transaction = ResaleTransactionEntity.create(
				order,
				item.listing(),
				generateResaleTicketNumber(resaleSuffix, "00" + (i + 1)),
				buyerId,
				item.getSellerId(),
				item.getListingPrice(),
				item.getBuyerFee(),
				item.getSellerFee(),
				item.getBuyerTotal(),
				item.getSellerTotal()
			);
			transactions.add(transaction);
		}
		return resaleTransactionRepository.saveAll(transactions);
	}

	private ResalePurchaseListResponse toPurchaseListResponse(
		ResaleOrderEntity order,
		List<ResaleTransactionEntity> transactions
	) {
		UUID gameId = transactions.isEmpty() ? null : transactions.getFirst().getListing().getGameId();
		List<String> seatInfos = transactions.stream()
			.map(transaction -> transaction.getListing().getSeatInfo())
			.toList();

		return ResalePurchaseListResponse.of(order, gameId, seatInfos);
	}

	private void validatePeriodFilter(
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		Preconditions.validate(
			months == null || (startDate == null && endDate == null),
			ErrorCode.ORDER_HISTORY_PERIOD_FILTER_CONFLICT
		);

		Preconditions.validate(
			(startDate == null) == (endDate == null),
			ErrorCode.ORDER_HISTORY_PERIOD_DATE_REQUIRED
		);

		if (months != null) {
			Preconditions.validate(
				ALLOWED_MONTHS.contains(months),
				ErrorCode.ORDER_HISTORY_PERIOD_MONTHS_INVALID
			);
		}

		if (startDate != null && endDate != null) {
			Preconditions.validate(
				!startDate.isAfter(endDate),
				ErrorCode.ORDER_HISTORY_PERIOD_INVALID_RANGE
			);
		}
	}
}
