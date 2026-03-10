package com.goti.domain.entity.order;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.goti.constants.OrderStatus;
import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.domain.entity.game.GameScheduleEntity;
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
	name = "orders",
	indexes = {
		@Index(name = "idx_orders_user_id", columnList = "user_id"),
		@Index(name = "idx_orders_game_schedule_id", columnList = "game_schedule_id")
	}
)
@NoArgsConstructor(access = PROTECTED)
public class OrderEntity extends ModificationTimestampEntity {

	@Column(nullable = false, unique = true)
	private String orderNumber;

	@Column(nullable = false)
	private UUID userId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_schedule_id", nullable = false)
	private GameScheduleEntity gameSchedule;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderStatus orderStatus;

	@Column(nullable = false)
	private Integer totalQuantity;

	@Column(nullable = false)
	private Integer totalAmount;

	private LocalDateTime confirmedAt;

	private LocalDateTime canceledAt;

	private OrderEntity(
		String orderNumber,
		UUID userId,
		GameScheduleEntity gameSchedule,
		Integer totalQuantity,
		Integer totalAmount
	) {
		this.orderNumber = orderNumber;
		this.userId = userId;
		this.gameSchedule = gameSchedule;
		this.orderStatus = OrderStatus.PENDING;
		this.totalQuantity = totalQuantity;
		this.totalAmount = totalAmount;
		this.confirmedAt = null;
		this.canceledAt = null;
	}

	public static OrderEntity create(
		String orderNumber,
		UUID userId,
		GameScheduleEntity gameSchedule,
		Integer totalQuantity,
		Integer totalAmount
	) {
		validate(orderNumber, userId, totalQuantity, totalAmount);
		return new OrderEntity(
			orderNumber,
			userId,
			gameSchedule,
			totalQuantity,
			totalAmount
		);
	}

	private static void validate(
		String orderNumber,
		UUID userId,
		Integer totalQuantity,
		Integer totalAmount
	) {
		Preconditions.domainValidate(
			StringUtils.hasText(orderNumber),
			"주문 번호는 비어 있을 수 없습니다."
		);
		Preconditions.domainValidate(
			userId != null,
			"유저 ID는 필수입니다."
		);
		Preconditions.domainValidate(
			totalQuantity != null && totalQuantity > 0,
			"총 수량은 0보다 커야 합니다."
		);
		Preconditions.domainValidate(
			totalAmount != null && totalAmount > 0,
			"총 금액은 0보다 커야 합니다."
		);
	}
}
