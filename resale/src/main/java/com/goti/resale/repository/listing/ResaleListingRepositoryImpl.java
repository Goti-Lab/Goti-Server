package com.goti.resale.repository.listing;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.goti.resale.constants.ResaleListingStatus;
import com.goti.resale.domain.entity.resale.QResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleListingEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ResaleListingRepositoryImpl implements ResaleListingRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public Page<ResaleListingEntity> findMySales(
		UUID sellerId,
		List<ResaleListingStatus> statuses,
		Integer months,
		LocalDate startDate,
		LocalDate endDate,
		Pageable pageable
	) {
		QResaleListingEntity listing = QResaleListingEntity.resaleListingEntity;

		List<ResaleListingEntity> content = queryFactory
			.selectFrom(listing)
			.where(
				listing.sellerId.eq(sellerId),
				statusCondition(listing, statuses),
				dateCondition(listing, months, startDate, endDate)
			)
			.orderBy(listing.createdAt.desc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(listing.count())
			.from(listing)
			.where(
				listing.sellerId.eq(sellerId),
				statusCondition(listing, statuses),
				dateCondition(listing, months, startDate, endDate)
			)
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0L);
	}

	private BooleanExpression statusCondition(QResaleListingEntity listing, List<ResaleListingStatus> statuses) {
		if (statuses == null || statuses.isEmpty()) {
			return null;
		}
		return listing.listingStatus.in(statuses);
	}

	private BooleanExpression dateCondition(
		QResaleListingEntity listing,
		Integer months,
		LocalDate startDate,
		LocalDate endDate
	) {
		if (startDate != null && endDate != null) {
			return listing.createdAt.between(
				toStartInstant(startDate),
				toEndInstant(endDate)
			);
		}

		if (months != null) {
			Instant from = toStartInstant(LocalDate.now().minusMonths(months));
			return listing.createdAt.goe(from);
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
