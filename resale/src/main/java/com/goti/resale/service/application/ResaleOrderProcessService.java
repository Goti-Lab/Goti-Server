package com.goti.resale.service.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.infra.lock.DistributedLockManager;
import com.goti.resale.constants.ResaleHoldStatus;
import com.goti.resale.constants.ResaleOrderStatus;
import com.goti.resale.domain.entity.resale.ResaleHoldEntity;
import com.goti.resale.domain.entity.resale.ResaleOrderEntity;
import com.goti.resale.domain.entity.resale.ResaleTransactionEntity;
import com.goti.resale.dto.response.ResaleOrderCompleteResponse;
import com.goti.resale.dto.response.ResaleOrderCreateResponse;
import com.goti.resale.dto.response.ResaleOrderListResponse;
import com.goti.resale.dto.response.ResalePurchaseListResponse;
import com.goti.resale.infra.dto.ResaleOrderPaymentCompletedEvent;
import com.goti.resale.infra.dto.SettlementCompletedEvent;
import com.goti.resale.repository.hold.ResaleHoldRepository;
import com.goti.resale.repository.order.ResaleOrderRepository;
import com.goti.resale.repository.transaction.ResaleTransactionRepository;
import com.goti.resale.service.domain.ResaleOrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResaleOrderProcessService {
	private static final String LOCK_KEY_PREFIX = "lock:resale:order:";

	private final ResaleOrderRepository resaleOrderRepository;
	private final ResaleTransactionRepository resaleTransactionRepository;
	private final ResaleHoldRepository resaleHoldRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final DistributedLockManager distributedLockManager;
	private final ResaleOrderService resaleOrderService;

	public ResaleOrderCreateResponse initOrder(
		UUID buyerId,
		List<UUID> holdIds,
		String buyerNickname,
		String buyerEmail,
		String buyerPhone
	) {
		List<ResaleHoldEntity> holds = validateAndGetHolds(buyerId, holdIds);
		UUID gameId = holds.getFirst().getResaleListing().getGameId();

		String lockKey = LOCK_KEY_PREFIX + buyerId + ":" + gameId;

		return distributedLockManager.withLock(
			lockKey,
			ErrorCode.PURCHASABLE_CHECK_FAILED,
			() -> resaleOrderService.initOrder(
				buyerId, holds, gameId,
				buyerNickname, buyerEmail, buyerPhone
			)
		);
	}

	@Transactional
	public ResaleOrderCompleteResponse completePayment(UUID resaleOrderId, UUID paymentId) {
		ResaleOrderEntity resaleOrder = resaleOrderRepository.findById(resaleOrderId)
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

		Preconditions.validate(
			resaleOrder.getOrderStatus() == ResaleOrderStatus.PENDING,
			ErrorCode.NOT_MATCH_STATUS, "결제 대기"
		);

		resaleOrder.complete();
		resaleOrderRepository.save(resaleOrder);

		List<ResaleTransactionEntity> transactions = resaleOrderService.findTransactionByOrder(resaleOrderId);
		for (ResaleTransactionEntity transaction : transactions) {
			transaction.complete(paymentId);
		}
		resaleTransactionRepository.saveAll(transactions);
		eventPublisher.publishEvent(new ResaleOrderPaymentCompletedEvent(
			resaleOrder.getId(),
			resaleOrder.getBuyerId(),
			paymentId
		));

		return ResaleOrderCompleteResponse.from(resaleOrder, transactions);
	}

	@Transactional
	public void completeSettlement(UUID resaleOrderId) {
		eventPublisher.publishEvent(new SettlementCompletedEvent(resaleOrderId));
		log.info("리셀 정산 완료 이벤트 발행 - 주문 ID: {}", resaleOrderId);
	}

	@Transactional(readOnly = true)
	public ResaleOrderListResponse getTransactionIds(UUID resaleOrderId) {
		List<UUID> transactions = resaleOrderService.findTransactionByOrder(resaleOrderId).stream()
			.map(ResaleTransactionEntity::getId)
			.toList();

		return new ResaleOrderListResponse(transactions);
	}

	@Transactional(readOnly = true)
	public List<ResalePurchaseListResponse> getPurchasesByMember(
		UUID buyerId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		return resaleOrderService.getPurchasesByMember(buyerId, months, startDate, endDate);
	}

	private List<ResaleHoldEntity> validateAndGetHolds(UUID buyerId, List<UUID> holdIds) {
		List<ResaleHoldEntity> holds = resaleHoldRepository
			.findAllByIdInAndUserIdAndStatus(holdIds, buyerId, ResaleHoldStatus.HOLDING);

		Preconditions.validate(
			holds.size() == holdIds.size(),
			ErrorCode.RESALE_HOLD_NOT_FOUND
		);

		for (ResaleHoldEntity hold : holds) {
			Preconditions.validate(
				hold.getExpiredAt().isAfter(LocalDateTime.now()),
				ErrorCode.RESALE_HOLD_EXPIRED
			);
		}
		return holds;
	}
}
