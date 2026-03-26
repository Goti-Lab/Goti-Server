package com.goti.resale.repository.hold;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.goti.resale.constants.ResaleHoldStatus;
import com.goti.resale.domain.entity.resale.QResaleHoldEntity;
import com.goti.resale.domain.entity.resale.QResaleListingEntity;
import com.goti.resale.domain.entity.resale.ResaleHoldEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ResaleHoldRepositoryImpl implements ResaleHoldRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final QResaleHoldEntity resaleHold = QResaleHoldEntity.resaleHoldEntity;
	private final QResaleListingEntity resaleListing = QResaleListingEntity.resaleListingEntity;

	@Override
	public List<ResaleHoldEntity> findExpiredResaleHolds(
		ResaleHoldStatus status,
		LocalDateTime now,
		Pageable pageable
	) {
		return queryFactory
			.selectFrom(resaleHold)
			.join(resaleHold.resaleListing, resaleListing).fetchJoin()
			.where(
				resaleHold.status.eq(status),
				resaleHold.expiredAt.before(now)
			)
			.orderBy(resaleHold.expiredAt.asc())
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();
	}
}
