package com.goti.ticketing.seat.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.goti.ticketing.constants.SeatHoldStatus;
import com.goti.ticketing.domain.entity.game.GameScheduleEntity;
import com.goti.ticketing.domain.entity.game.QGameScheduleEntity;
import com.goti.ticketing.domain.entity.seat.SeatEntity;
import com.goti.ticketing.domain.entity.seat.QSeatEntity;
import com.goti.ticketing.domain.entity.seat.QSeatGradeEntity;
import com.goti.ticketing.domain.entity.seat.QSeatHoldEntity;
import com.goti.ticketing.domain.entity.seat.QSeatSectionEntity;
import com.goti.ticketing.domain.entity.seat.SeatHoldEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SeatHoldRepositoryImpl implements SeatHoldRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<SeatHoldEntity> findAllWithDetailsByIdIn(List<UUID> holdIds) {
		QSeatHoldEntity seatHold = QSeatHoldEntity.seatHoldEntity;
		QGameScheduleEntity gameSchedule = QGameScheduleEntity.gameScheduleEntity;
		QSeatEntity seat = QSeatEntity.seatEntity;
		QSeatSectionEntity seatSection = QSeatSectionEntity.seatSectionEntity;
		QSeatGradeEntity seatGrade = QSeatGradeEntity.seatGradeEntity;

		return queryFactory
			.selectFrom(seatHold)
			.join(seatHold.gameSchedule, gameSchedule).fetchJoin()
			.join(seatHold.seat, seat).fetchJoin()
			.join(seat.seatSection, seatSection).fetchJoin()
			.join(seatSection.seatGrade, seatGrade).fetchJoin()
			.where(seatHold.id.in(holdIds))
			.fetch();
	}

	@Override
	public List<SeatHoldEntity> findHoldsWithSeatAndGame(SeatHoldStatus status, LocalDateTime now, int limit) {
		QSeatHoldEntity seatHold = QSeatHoldEntity.seatHoldEntity;
		QGameScheduleEntity gameSchedule = QGameScheduleEntity.gameScheduleEntity;
		QSeatEntity seat = QSeatEntity.seatEntity;

		return queryFactory
			.selectFrom(seatHold)
			.join(seatHold.gameSchedule, gameSchedule).fetchJoin()
			.join(seatHold.seat, seat).fetchJoin()
			.where(
				seatHold.status.eq(status),
				seatHold.expiredAt.before(now)
			)
			.orderBy(seatHold.expiredAt.asc())
			.limit(limit)
			.fetch();
	}

	@Override
	public Optional<SeatHoldEntity> findHoldWithSeatAndGame(UUID holdId) {
		QSeatHoldEntity seatHold = QSeatHoldEntity.seatHoldEntity;
		QGameScheduleEntity gameSchedule = QGameScheduleEntity.gameScheduleEntity;
		QSeatEntity seat = QSeatEntity.seatEntity;

		return Optional.ofNullable(
			queryFactory
				.selectFrom(seatHold)
				.join(seatHold.gameSchedule, gameSchedule).fetchJoin()
				.join(seatHold.seat, seat).fetchJoin()
				.where(seatHold.id.eq(holdId))
				.fetchOne()
		);
	}

	@Override
	public List<SeatHoldEntity> findAllHoldingSeats(UUID gameId, UUID userId) {
		QSeatHoldEntity seatHold = QSeatHoldEntity.seatHoldEntity;
		QGameScheduleEntity gameSchedule = QGameScheduleEntity.gameScheduleEntity;
		QSeatEntity seat = QSeatEntity.seatEntity;
		QSeatSectionEntity seatSection = QSeatSectionEntity.seatSectionEntity;
		QSeatGradeEntity seatGrade = QSeatGradeEntity.seatGradeEntity;

		return queryFactory
			.selectFrom(seatHold)
			.join(seatHold.gameSchedule, gameSchedule).fetchJoin()
			.join(seatHold.seat, seat).fetchJoin()
			.join(seat.seatSection, seatSection).fetchJoin()
			.join(seatSection.seatGrade, seatGrade).fetchJoin()
			.where(
				seatHold.gameSchedule.id.eq(gameId),
				seatHold.userId.eq(userId),
				seatHold.status.eq(SeatHoldStatus.HOLDING)
			)
			.fetch();
	}
}
