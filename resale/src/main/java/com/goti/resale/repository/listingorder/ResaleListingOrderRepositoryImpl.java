package com.goti.resale.repository.listingorder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.goti.resale.constants.ResaleListingOrderStatus;
import com.goti.resale.domain.entity.resale.QResaleListingOrderEntity;
import com.goti.resale.domain.entity.resale.ResaleListingOrderEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ResaleListingOrderRepositoryImpl implements ResaleListingOrderRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<ResaleListingOrderEntity> getSalesHistory(
		UUID sellerId,
		List<ResaleListingOrderStatus> statuses,
		Integer months,
		LocalDate startDate,
		LocalDate endDate,
		Pageable pageable
	) {
		QResaleListingOrderEntity listingOrder = QResaleListingOrderEntity.resaleListingOrderEntity;

		List<ResaleListingOrderEntity> content = queryFactory
			.selectFrom(listingOrder)
			.where(
				listingOrder.sellerId.eq(sellerId),
				statusCondition(listingOrder, statuses),
				dateCondition(listingOrder, months, startDate, endDate)
			)
			.orderBy(listingOrder.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(listingOrder.count())
			.from(listingOrder)
			.where(
				listingOrder.sellerId.eq(sellerId),
				statusCondition(listingOrder, statuses),
				dateCondition(listingOrder, months, startDate, endDate)
			)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	private BooleanExpression statusCondition(QResaleListingOrderEntity listingOrder,
		List<ResaleListingOrderStatus> statuses) {
		if (statuses == null || statuses.isEmpty()) {
			return null;
		}
		return listingOrder.orderStatus.in(statuses);
	}

	private BooleanExpression dateCondition(
		QResaleListingOrderEntity listingOrder,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		if (startDate != null && endDate != null) {
			return listingOrder.createdAt.between(
				toStartAt(startDate),
				toEndAt(endDate)
			);
		}

		if (months != null) {
			Instant from = toStartAt(LocalDate.now().minusMonths(months));
			return listingOrder.createdAt.goe(from);
		}

		return null;
	}

	private Instant toStartAt(LocalDate date) {
		return date.atStartOfDay().toInstant(ZoneOffset.UTC);
	}

	private Instant toEndAt(LocalDate date) {
		return date.plusDays(1).atStartOfDay().minusNanos(1).toInstant(ZoneOffset.UTC);
	}
}
