package com.goti.seat.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.goti.domain.entity.seat.QSeatEntity;
import com.goti.domain.entity.seat.SeatEntity;
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
}
