package com.goti.ticketing.game.repository.gameschedule;

import com.goti.ticketing.domain.entity.game.QGameScheduleEntity;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.goti.ticketing.domain.entity.game.QGameScheduleEntity.gameScheduleEntity;

@Repository
@RequiredArgsConstructor
public class GameScheduleRepositoryImpl implements GameScheduleRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;
	private final QGameScheduleEntity gameSchedule = gameScheduleEntity;

	@Override
	public boolean existsDuplicateSchedule(
		UUID homeTeamId,
		UUID awayTeamId,
		LocalDateTime startAt
	) {
		return jpaQueryFactory
			.selectOne()
			.from(gameSchedule)
			.where(
				gameSchedule.startAt.eq(startAt),
				isAnyTeamInvolved(homeTeamId, awayTeamId)
			)
			.fetchFirst() != null;
	}

	private BooleanExpression isAnyTeamInvolved(UUID homeId, UUID awayId) {
		return gameSchedule.homeTeamId.in(homeId, awayId)
			.or(gameSchedule.awayTeamId.in(homeId, awayId));
	}
}
