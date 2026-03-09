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
	private final QResalePriceHistoryEntity resalePriceHistory = QResalePriceHistoryEntity.resalePriceHistoryEntity;

	@Override
	public Optional<ResalePriceHistoryEntity> findByGameAndGrade(UUID gameId, UUID gradeId) {
		return Optional.ofNullable(
			queryFactory
				.selectFrom(resalePriceHistory)
				.where(
					resalePriceHistory.gameId.eq(gameId),
					resalePriceHistory.gradeId.eq(gradeId)
				)
				.orderBy(resalePriceHistory.transactionTime.desc())
				.fetchFirst()
		);
	}

	@Override
	public Optional<ResalePriceHistoryEntity> findByGameAndGradeAndDate(
		UUID gameId,
		UUID gradeId,
		LocalDate transactionDate
	) {
		return Optional.ofNullable(
			queryFactory
				.selectFrom(resalePriceHistory)
				.where(
					resalePriceHistory.gameId.eq(gameId),
					resalePriceHistory.gradeId.eq(gradeId),
					resalePriceHistory.transactionDate.eq(transactionDate)
				)
				.orderBy(resalePriceHistory.transactionTime.desc())
				.fetchFirst()
		);
	}
}
