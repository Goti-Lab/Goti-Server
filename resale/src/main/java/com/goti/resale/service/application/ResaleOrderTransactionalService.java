package com.goti.resale.service.application;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.f4b6a3.tsid.TsidCreator;
import com.goti.domain.vo.TransactionItemVO;
import com.goti.dto.internal.ResaleOrderCreatedEvent;
import com.goti.resale.constants.ResaleTransactionStatus;
import com.goti.resale.domain.entity.resale.ResaleHoldEntity;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleOrderEntity;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;
import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;
import com.goti.resale.dto.request.ResaleTransactionItemRequest;
import com.goti.resale.dto.response.ResaleOrderCreateResponse;
import com.goti.resale.infra.TicketClient;
import com.goti.resale.repository.ResaleOrderRepository;
import com.goti.resale.repository.ResaleTransactionRepository;
import com.goti.resale.utils.ResalePricePolicy;
import com.goti.resale.utils.ResaleRestrictionHandler;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResaleOrderTransactionalService {
	private static final DateTimeFormatter ORDER_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
	private static final DateTimeFormatter TICKET_NUMBER_FORMATTER = DateTimeFormatter.ofPattern("MMdd");

	private final ResaleOrderRepository resaleOrderRepository;
	private final ResaleTransactionRepository resaleTransactionRepository;
	private final ResaleRestrictionHandler resaleRestrictionHandler;
	private final ResaleRestrictionService restrictionService;
	private final ResalePricePolicy resalePricePolicy;
	private final TicketClient ticketClient;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public ResaleOrderCreateResponse initOrder(
		UUID buyerId,
		List<ResaleHoldEntity> holds,
		UUID gameId
	) {
		int ownedCount = ticketClient.getOwnedTicketCount(buyerId, gameId);
		int pendingCount = resaleTransactionRepository.countByBuyerIdAndListing_GameIdAndTransactionStatus(
			buyerId, gameId, ResaleTransactionStatus.PENDING);

		resaleRestrictionHandler.validatePossessionLimit(ownedCount, pendingCount, holds.size());

		List<TransactionItemVO> itemVOs = calculateOrderItems(buyerId, holds);

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

	private List<TransactionItemVO> calculateOrderItems(UUID buyerId, List<ResaleHoldEntity> holds) {
		List<TransactionItemVO> itemVOs = new ArrayList<>();
		for (ResaleHoldEntity hold : holds) {
			ResaleListingEntity listing = hold.getResaleListing();

			ResaleRestrictionEntity restriction = restrictionService.getOrCreateRestriction(buyerId);
			resaleRestrictionHandler.validateCanBuy(restriction, listing.getGameId());

			ResalePricePolicy.FeeResult feeResult = resalePricePolicy.validateTransactionFee(
				listing.getListingPrice()
			);
			itemVOs.add(new TransactionItemVO(listing, feeResult));
		}
		return itemVOs;
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
			String num = String.format("%03d", i + 1);
			TransactionItemVO item = itemVOs.get(i);
			ResaleTransactionEntity transaction = ResaleTransactionEntity.create(
				order,
				item.listing(),
				generateResaleTicketNumber(resaleSuffix, num),
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

	private String generateOrderNumber() {
		String tsidSuffix = TsidCreator.getTsid().toString();
		return "ORD" + "-" +
			LocalDate.now().format(ORDER_NUMBER_FORMATTER) +
			tsidSuffix.substring(tsidSuffix.length() - 6);
	}

	private String generateResaleTicketNumber(String resaleSuffix, String num) {
		return "RST" + "-" +
			LocalDate.now().format(TICKET_NUMBER_FORMATTER) +
			resaleSuffix +
			"-" + num;
	}
}
