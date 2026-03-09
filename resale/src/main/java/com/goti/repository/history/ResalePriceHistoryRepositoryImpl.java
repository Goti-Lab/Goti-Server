package com.goti.repository.history;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.goti.domain.entity.resale.QResalePriceHistoryEntity;
import com.goti.domain.entity.resale.ResalePriceHistoryEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ResalePriceHistoryRepositoryImpl implements ResalePriceHistoryRepositoryCustom {

	private final JPAQueryFactory queryFactory;
	private final QResalePriceHistoryEntity resalePriceHistoryEntity = QResalePriceHistoryEntity.resalePriceHistoryEntity;

	@Override
	public Optional<ResalePriceHistoryEntity> findLatestByGradeId(UUID gradeId) {
		return Optional.ofNullable(
			queryFactory
				.selectFrom(resalePriceHistoryEntity)
				.where(resalePriceHistoryEntity.gradeId.eq(gradeId))
				.orderBy(resalePriceHistoryEntity.transactionTime.desc())
				.fetchFirst()
		);
	}

	@Override
	public Optional<ResalePriceHistoryEntity> findLatestByGameIdAndGradeIdAndDate(
		UUID gameId,
		UUID gradeId,
		LocalDate transactionDate
	) {
		return Optional.ofNullable(
			queryFactory
				.selectFrom(resalePriceHistoryEntity)
				.where(
					resalePriceHistoryEntity.gameId.eq(gameId),
					resalePriceHistoryEntity.gradeId.eq(gradeId),
					resalePriceHistoryEntity.transactionDate.eq(transactionDate)
				)
				.orderBy(resalePriceHistoryEntity.transactionTime.desc())
				.fetchFirst()
		);
	}
}
