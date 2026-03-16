package com.goti.ticketing.domain.entity.order;

import static lombok.AccessLevel.*;

import com.goti.ticketing.constants.OrderItemStatus;
import com.goti.ticketing.constants.TicketType;
import com.goti.domain.base.CreationTimestampEntity;
import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.ticketing.domain.entity.seat.SeatEntity;
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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "order_items",
	indexes = {
		@Index(name = "idx_order_items_order_id", columnList = "order_id")
	},
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_order_items_order_id_seat_id", columnNames = {"order_id", "seat_id"})
	}
)
@NoArgsConstructor(access = PROTECTED)
public class OrderItemEntity extends ModificationTimestampEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private OrderEntity order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_id", nullable = false)
	private SeatEntity seat;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TicketType ticketType;

	@Column(nullable = false)
	private Integer ticketPrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private OrderItemStatus itemStatus;

	private OrderItemEntity(
		OrderEntity order,
		SeatEntity seat,
		TicketType ticketType,
		Integer ticketPrice
	) {
		this.order = order;
		this.seat = seat;
		this.ticketType = ticketType;
		this.ticketPrice = ticketPrice;
		this.itemStatus = OrderItemStatus.RESERVED;
	}

	public static OrderItemEntity create(
		OrderEntity order,
		SeatEntity seat,
		TicketType ticketType,
		Integer ticketPrice
	) {
		validate(ticketType, ticketPrice);
		return new OrderItemEntity(order, seat, ticketType, ticketPrice);
	}

	public void pay() {
		Preconditions.domainValidate(
			this.itemStatus == OrderItemStatus.RESERVED,
			"RESERVED 상태에서만 결제 완료 처리할 수 있습니다."
		);
		this.itemStatus = OrderItemStatus.PAID;
	}

	private static void validate(
		TicketType ticketType,
		Integer ticketPrice
	) {
		Preconditions.domainValidate(
			ticketType != null,
			"권종은 필수입니다."
		);
		Preconditions.domainValidate(
			ticketPrice != null && ticketPrice >= 0,
			"티켓 가격은 0원 보다 커야합니다"
		);
	}
}
