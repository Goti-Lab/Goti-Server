package com.goti.ticketing.order.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.goti.ticketing.domain.entity.game.QGameScheduleEntity;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.order.QOrderEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<OrderEntity> findMyOrders(
		UUID memberId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		QOrderEntity order = QOrderEntity.orderEntity;
		QGameScheduleEntity gameSchedule = QGameScheduleEntity.gameScheduleEntity;

		return queryFactory
			.selectFrom(order)
			.join(order.gameSchedule, gameSchedule).fetchJoin()
			.where(
				order.memberId.eq(memberId),
				dateCondition(order, months, startDate, endDate)
			)
			.orderBy(order.createdAt.desc())
			.fetch();
	}

	private BooleanExpression dateCondition(
		QOrderEntity order,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		if (startDate != null && endDate != null) {
			return order.createdAt.between(
				toStartInstant(startDate),
				toEndInstant(endDate)
			);
		}

		if (months != null) {
			Instant from = toStartInstant(LocalDate.now().minusMonths(months));
			return order.createdAt.goe(from);
		}

		return null;
	}

	private Instant toStartInstant(LocalDate date) {
		return date.atStartOfDay().toInstant(ZoneOffset.UTC);
	}

	private Instant toEndInstant(LocalDate date) {
		return date.plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC);
	}
}
