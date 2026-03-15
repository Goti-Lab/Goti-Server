package com.goti.order.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import com.goti.domain.entity.seat.QSeatSectionEntity;

import org.springframework.stereotype.Repository;

import com.goti.constants.OrderItemStatus;
import com.goti.domain.entity.game.QGameScheduleEntity;
import com.goti.domain.entity.order.OrderItemEntity;
import com.goti.domain.entity.order.QOrderEntity;
import com.goti.domain.entity.order.QOrderItemEntity;
import com.goti.domain.entity.seat.QSeatEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderItemRepositoryImpl implements OrderItemRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public boolean existsOrderedSeats(
		UUID gameId,
		Collection<UUID> seatIds,
		Collection<OrderItemStatus> statuses
	) {
		QOrderItemEntity orderItem = QOrderItemEntity.orderItemEntity;
		QOrderEntity order = QOrderEntity.orderEntity;
		QGameScheduleEntity gameSchedule = QGameScheduleEntity.gameScheduleEntity;
		QSeatEntity seat = QSeatEntity.seatEntity;

		OrderItemEntity result = queryFactory
			.selectFrom(orderItem)
			.join(orderItem.order, order)
			.join(order.gameSchedule, gameSchedule)
			.join(orderItem.seat, seat)
			.where(
				gameSchedule.id.eq(gameId),
				seat.id.in(seatIds),
				orderItem.itemStatus.in(statuses)
			)
			.fetchFirst();

		return result != null;
	}

	@Override
	public List<OrderItemEntity> findOrderItemsByOrderId(UUID orderId) {
		QOrderItemEntity orderItem = QOrderItemEntity.orderItemEntity;
		QOrderEntity order = QOrderEntity.orderEntity;
		QSeatEntity seat = QSeatEntity.seatEntity;
		QSeatSectionEntity seatSection = QSeatSectionEntity.seatSectionEntity;

		return queryFactory
			.selectFrom(orderItem)
			.join(orderItem.order, order).fetchJoin()
			.join(orderItem.seat, seat).fetchJoin()
			.join(seat.seatSection, seatSection).fetchJoin()
			.where(order.id.eq(orderId))
			.fetch();
	}
}
