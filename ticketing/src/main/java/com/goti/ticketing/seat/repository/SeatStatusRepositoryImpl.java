package com.goti.ticketing.seat.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.goti.ticketing.domain.entity.seat.QSeatEntity;
import com.goti.ticketing.domain.entity.seat.QSeatStatusEntity;
import com.goti.ticketing.domain.entity.seat.SeatStatusEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SeatStatusRepositoryImpl implements SeatStatusRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<SeatStatusEntity> findSeatStatuses(UUID gameId, List<UUID> seatIds) {
		QSeatStatusEntity seatStatus = QSeatStatusEntity.seatStatusEntity;
		QSeatEntity seat = QSeatEntity.seatEntity;

		return queryFactory
			.selectFrom(seatStatus)
			.join(seatStatus.seat, seat).fetchJoin()
			.where(
				seatStatus.game.id.eq(gameId),
				seat.id.in(seatIds)
			)
			.fetch();
	}

	@Override
	public List<UUID> findSeatIdsByGameId(UUID gameId) {
		QSeatStatusEntity seatStatus = QSeatStatusEntity.seatStatusEntity;

		return queryFactory
			.select(seatStatus.seat.id)
			.from(seatStatus)
			.where(seatStatus.game.id.eq(gameId))
			.fetch();
	}
}
