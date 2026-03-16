package com.goti.domain.entity.order;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
	name = "order_cancellation_items",
	indexes = {
		@Index(name = "idx_order_cancellation_items_cancellation_id", columnList = "cancellation_id"),
		@Index(name = "idx_order_cancellation_items_item_id", columnList = "item_id")
	}
)
@NoArgsConstructor(access = PROTECTED)
public class OrderCancellationItemEntity extends ModificationTimestampEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cancellation_id", nullable = false)
	private OrderCancellationEntity cancellation;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", nullable = false)
	private OrderItemEntity item;

	@Column(nullable = false)
	private Integer refundAmount;

	@Column(nullable = false)
	private Integer feeAmount;

	private LocalDateTime cancelledAt;

	private OrderCancellationItemEntity(
		OrderCancellationEntity cancellation,
		OrderItemEntity item,
		Integer refundAmount,
		Integer feeAmount
	) {
		this.cancellation = cancellation;
		this.item = item;
		this.refundAmount = refundAmount;
		this.feeAmount = feeAmount;
		this.cancelledAt = null;
	}

	public static OrderCancellationItemEntity create(
		OrderCancellationEntity cancellation,
		OrderItemEntity item,
		Integer refundAmount,
		Integer feeAmount
	) {
		validate(refundAmount, feeAmount);
		return new OrderCancellationItemEntity(
			cancellation,
			item,
			refundAmount,
			feeAmount
		);
	}

	private static void validate(
		Integer refundAmount,
		Integer feeAmount
	) {
		Preconditions.domainValidate(
			refundAmount != null && refundAmount >= 0,
			"환불액은 0 이상이어야 합니다."
		);
		Preconditions.domainValidate(
			feeAmount != null && feeAmount >= 0,
			"수수료는 0 이상이어야 합니다."
		);
	}
}
