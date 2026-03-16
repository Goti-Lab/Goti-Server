package com.goti.ticketing.domain.entity.order;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.goti.ticketing.constants.OrderCancelDenyReason;
import com.goti.ticketing.constants.OrderCancellationRequestType;
import com.goti.ticketing.constants.OrderCancellationStatus;
import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "order_cancellations",
	indexes = {
		@Index(name = "idx_order_cancellations_order_id", columnList = "order_id")
	}
)
@NoArgsConstructor(access = PROTECTED)
public class OrderCancellationEntity extends ModificationTimestampEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private OrderEntity order;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderCancellationStatus status;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderCancellationRequestType requestType;

	@Enumerated(EnumType.STRING)
	private OrderCancelDenyReason denyReasonCode;

	@Column(nullable = false)
	private UUID requestedBy;

	private UUID approvedBy;

	private LocalDateTime approvedAt;

	@Column(nullable = false)
	private Integer refundAmountTotal;

	@Column(nullable = false)
	private Integer feeAmountTotal;

	private LocalDateTime completedAt;

	@Column(nullable = false, unique = true)
	private String idempotencyKey;

	private OrderCancellationEntity(
		OrderEntity order,
		OrderCancellationRequestType requestType,
		UUID requestedBy,
		Integer refundAmountTotal,
		Integer feeAmountTotal,
		String idempotencyKey
	) {
		this.order = order;
		this.status = OrderCancellationStatus.REQUESTED;
		this.requestType = requestType;
		this.denyReasonCode = null;
		this.requestedBy = requestedBy;
		this.approvedBy = null;
		this.approvedAt = null;
		this.refundAmountTotal = refundAmountTotal;
		this.feeAmountTotal = feeAmountTotal;
		this.completedAt = null;
		this.idempotencyKey = idempotencyKey;
	}

	public static OrderCancellationEntity create(
		OrderEntity order,
		OrderCancellationRequestType requestType,
		UUID requestedBy,
		Integer refundAmountTotal,
		Integer feeAmountTotal,
		String idempotencyKey
	) {
		validate(
			requestType,
			requestedBy,
			refundAmountTotal,
			feeAmountTotal,
			idempotencyKey
		);
		return new OrderCancellationEntity(
			order,
			requestType,
			requestedBy,
			refundAmountTotal,
			feeAmountTotal,
			idempotencyKey
		);
	}

	private static void validate(
		OrderCancellationRequestType requestType,
		UUID requestedBy,
		Integer refundAmountTotal,
		Integer feeAmountTotal,
		String idempotencyKey
	) {
		Preconditions.domainValidate(
			requestType != null,
			"취소 요청 타입은 필수입니다."
		);
		Preconditions.domainValidate(
			requestedBy != null,
			"요청자는 필수입니다."
		);
		Preconditions.domainValidate(
			refundAmountTotal != null && refundAmountTotal >= 0,
			"총 환불액은 0 이상이어야 합니다."
		);
		Preconditions.domainValidate(
			feeAmountTotal != null && feeAmountTotal >= 0,
			"총 수수료는 0 이상이어야 합니다."
		);
		Preconditions.domainValidate(
			StringUtils.hasText(idempotencyKey),
			"멱등 키는 비어 있을 수 없습니다."
		);
	}
}
