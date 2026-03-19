package com.goti.ticketing.seat.repository;

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
	public Optional<SeatHoldEntity> findLatestActiveHold(
		GameScheduleEntity gameSchedule,
		SeatEntity seat,
		UUID userId,
		SeatHoldStatus status
	) {
		QSeatHoldEntity seatHold = QSeatHoldEntity.seatHoldEntity;

		return Optional.ofNullable(
			queryFactory
				.selectFrom(seatHold)
				.where(
					seatHold.gameSchedule.eq(gameSchedule),
					seatHold.seat.eq(seat),
					seatHold.userId.eq(userId),
					seatHold.status.eq(status)
				)
				.orderBy(seatHold.createdAt.desc())
				.fetchFirst()
		);
	}
}
