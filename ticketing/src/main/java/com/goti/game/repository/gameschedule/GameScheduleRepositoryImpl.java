package com.goti.game.repository.gameschedule;

import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.game.QGameScheduleEntity;
import com.goti.domain.entity.game.QGameStatusEntity;
import com.goti.domain.entity.game.QGameTicketingStatusEntity;
import com.goti.game.dto.request.GameScheduleSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static com.goti.domain.entity.game.QGameScheduleEntity.gameScheduleEntity;

@Repository
@RequiredArgsConstructor
public class GameScheduleRepositoryImpl implements GameScheduleRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;
	private final QGameScheduleEntity gameSchedule = gameScheduleEntity;
	private final QGameStatusEntity gameStatus = QGameStatusEntity.gameStatusEntity;
	private final QGameTicketingStatusEntity ticketingStatus = QGameTicketingStatusEntity.gameTicketingStatusEntity;
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

	@Override
	public List<GameScheduleEntity> searchSchedules(GameScheduleSearchCondition condition) {
		return jpaQueryFactory
			.selectFrom(gameSchedule)
			.leftJoin(gameStatus).on(gameStatus.gameSchedule.eq(gameSchedule)).fetchJoin()
			.leftJoin(ticketingStatus).on(ticketingStatus.gameSchedule.eq(gameSchedule)).fetchJoin()
			.where(
				teamIdEq(gameSchedule, condition.teamId()),
				dateFilter(gameSchedule, condition)
			)
			.orderBy(gameSchedule.startAt.asc())
			.fetch();
	}

	private BooleanExpression teamIdEq(QGameScheduleEntity game, UUID teamId) {
		if (teamId == null) return null;
		return game.homeTeamId.eq(teamId).or(game.awayTeamId.eq(teamId));
	}

	private BooleanExpression dateFilter(QGameScheduleEntity game, GameScheduleSearchCondition condition) {
		if (condition.today()) {
			LocalDate today = LocalDate.now();
			return game.startAt.between(today.atStartOfDay(), today.atTime(LocalTime.MAX));
		}

		if (condition.year() != null && condition.month() != null) {
			LocalDateTime startOfMonth = LocalDateTime.of(condition.year(), condition.month(), 1, 0, 0);

			if (condition.week() != null) {
				LocalDateTime startOfWeek = startOfMonth.plusWeeks(condition.week() - 1);
				LocalDateTime endOfWeek = startOfWeek.plusDays(6).with(LocalTime.MAX);
				return game.startAt.between(startOfWeek, endOfWeek);
			}

			return game.startAt.between(startOfMonth, startOfMonth.plusMonths(1).minusNanos(1));
		}
		return null;
	}

	private BooleanExpression isAnyTeamInvolved(UUID homeId, UUID awayId) {
		return gameSchedule.homeTeamId.in(homeId, awayId)
			.or(gameSchedule.awayTeamId.in(homeId, awayId));
	}
}
