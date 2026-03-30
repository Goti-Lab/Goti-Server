package com.goti.ticketing.seat.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.goti.ticketing.domain.entity.seat.QSeatEntity;
import com.goti.ticketing.domain.entity.seat.QSeatSectionEntity;
import com.goti.ticketing.domain.entity.seat.SeatEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SeatRepositoryImpl implements SeatRepositoryCustom {
	private final JPAQueryFactory queryFactory;

	@Override
	public List<SeatEntity> findAllBySection(UUID sectionId) {
		QSeatEntity seat = QSeatEntity.seatEntity;

		return queryFactory
			.selectFrom(seat)
			.join(seat.seatSection).fetchJoin()
			.where(seat.seatSection.id.eq(sectionId))
			.orderBy(seat.rowName.asc(), seat.seatNum.asc())
			.fetch();
	}

	@Override
	public List<SeatEntity> findAllByStadiumId(UUID stadiumId) {
		QSeatEntity seat = QSeatEntity.seatEntity;
		QSeatSectionEntity seatSection = QSeatSectionEntity.seatSectionEntity;

		return queryFactory
			.selectFrom(seat)
			.join(seat.seatSection, seatSection).fetchJoin()
			.where(seatSection.stadiumId.eq(stadiumId))
			.orderBy(seat.id.asc())
			.fetch();
	}
}
