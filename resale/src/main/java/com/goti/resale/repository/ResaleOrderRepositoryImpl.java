package com.goti.resale.repository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.goti.resale.constants.ResaleOrderStatus;
import com.goti.resale.domain.entity.resale.QResaleOrderEntity;
import com.goti.resale.domain.entity.resale.ResaleOrderEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ResaleOrderRepositoryImpl implements ResaleOrderRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<ResaleOrderEntity> findCompletedPurchaseOrders(
		UUID buyerId,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		QResaleOrderEntity resaleOrder = QResaleOrderEntity.resaleOrderEntity;

		return queryFactory
			.selectFrom(resaleOrder)
			.where(
				resaleOrder.buyerId.eq(buyerId),
				resaleOrder.orderStatus.eq(ResaleOrderStatus.COMPLETED),
				dateCondition(resaleOrder, months, startDate, endDate)
			)
			.orderBy(resaleOrder.createdAt.desc())
			.fetch();
	}

	private BooleanExpression dateCondition(
		QResaleOrderEntity resaleOrder,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		if (startDate != null && endDate != null) {
			return resaleOrder.createdAt.between(
				toStartInstant(startDate),
				toEndInstant(endDate)
			);
		}

		if (months != null) {
			Instant from = toStartInstant(LocalDate.now().minusMonths(months));
			return resaleOrder.createdAt.goe(from);
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
