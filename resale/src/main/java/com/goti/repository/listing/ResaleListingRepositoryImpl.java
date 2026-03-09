package com.goti.repository.listing;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.goti.constants.ResaleListingStatus;
import com.goti.domain.entity.resale.QResaleListingEntity;
import com.goti.domain.entity.resale.ResaleListingEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ResaleListingRepositoryImpl implements ResaleListingRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final QResaleListingEntity resaleListing = QResaleListingEntity.resaleListingEntity;

	@Override
	public boolean existsByTicketIdAndListingStatusIn(UUID ticketId, List<ResaleListingStatus> statuses) {
		Integer fetchOne = queryFactory
			.selectOne()
			.from(resaleListing)
			.where(
				resaleListing.ticketId.eq(ticketId),
				resaleListing.listingStatus.in(statuses)
			)
			.fetchFirst();
		return fetchOne != null;
	}

	@Override
	public List<ResaleListingEntity> findByGameAndListingStatusIn(UUID gameId, List<ResaleListingStatus> statuses) {
		return queryFactory
			.selectFrom(resaleListing)
			.where(
				resaleListing.gameId.eq(gameId),
				resaleListing.listingStatus.in(statuses)
			)
			.fetch();
	}

	@Override
	public List<ResaleListingEntity> findByGameAndGradeAndListingStatus(
		UUID gameId,
		UUID gradeId,
		ResaleListingStatus listingStatus
	) {
		return queryFactory
			.selectFrom(resaleListing)
			.where(
				resaleListing.gameId.eq(gameId),
				resaleListing.gradeId.eq(gradeId),
				resaleListing.listingStatus.eq(listingStatus)
			)
			.fetch();
	}
}
