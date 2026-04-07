package com.goti.ticketing.game.repository.gameschedule;

import static com.goti.ticketing.domain.entity.game.QGameScheduleEntity.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.goti.ticketing.constants.SeatStatus;
import com.goti.ticketing.domain.entity.game.QGameScheduleEntity;
import com.goti.ticketing.domain.entity.game.QGameStatusEntity;
import com.goti.ticketing.domain.entity.game.QGameTicketingStatusEntity;
import com.goti.ticketing.domain.entity.seat.QSeatStatusEntity;
import com.goti.ticketing.game.dto.request.GameScheduleSearchCondition;
import com.goti.ticketing.game.dto.response.GameScheduleSearchResponse;
import com.goti.ticketing.game.dto.response.QGameScheduleSearchResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GameScheduleRepositoryImpl implements GameScheduleRepositoryCustom {

	private final JPAQueryFactory jpaQueryFactory;
	private final QGameScheduleEntity gameSchedule = gameScheduleEntity;
	private final QGameStatusEntity gameStatus = QGameStatusEntity.gameStatusEntity;
	private final QGameTicketingStatusEntity ticketingStatus = QGameTicketingStatusEntity.gameTicketingStatusEntity;
	private final QSeatStatusEntity seatStatus = QSeatStatusEntity.seatStatusEntity;

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
	public List<GameScheduleSearchResponse> searchSchedules(GameScheduleSearchCondition condition) {
		LocalDateTime now = LocalDateTime.now();
		return jpaQueryFactory
			.select(
				new QGameScheduleSearchResponse(
					gameSchedule.id,
					gameSchedule.startAt,
					gameSchedule.leagueType,
					gameSchedule.homeTeamId,
					gameSchedule.awayTeamId,
					gameSchedule.stadiumId,
					Expressions.nullExpression(String.class),
					Expressions.nullExpression(String.class),
					Expressions.nullExpression(String.class),
					gameStatus.gameStatus,
					gameStatus.homeTeamScore,
					gameStatus.awayTeamScore,
					gameStatus.gameResult,
					ticketingStatus.status,
					ticketingStatus.ticketingOpenedAt,
					ticketingStatus.ticketingEndAt,
					Expressions.constant(0L)
				)
			)
			.from(gameSchedule)
			.leftJoin(gameStatus).on(gameStatus.gameSchedule.eq(gameSchedule))
			.leftJoin(ticketingStatus).on(ticketingStatus.gameSchedule.eq(gameSchedule))
			.where(
				teamIdEq(gameSchedule, condition.teamId()),
				dateFilter(gameSchedule, condition, now)
			)
			.orderBy(gameSchedule.startAt.asc())
			.fetch();
	}

	@Override
	public Map<UUID, Long> findRemainingSeatCounts(List<UUID> scheduleIds) {
		return jpaQueryFactory
			.select(seatStatus.game.id, seatStatus.count())
			.from(seatStatus)
			.where(
				seatStatus.game.id.in(scheduleIds),
				seatStatus.status.eq(SeatStatus.AVAILABLE)
			)
			.groupBy(seatStatus.game.id)
			.fetch()
			.stream()
			.collect(Collectors.toMap(
				tuple -> tuple.get(seatStatus.game.id),
				tuple -> tuple.get(seatStatus.count())
			));
	}



	@Override
	public Optional<GameScheduleSearchResponse> findScheduleByGameId(UUID gameId) {
		return Optional.ofNullable(
			jpaQueryFactory
				.select(
					new QGameScheduleSearchResponse(
						gameSchedule.id,
						gameSchedule.startAt,
						gameSchedule.leagueType,
						gameSchedule.homeTeamId,
						gameSchedule.awayTeamId,
						gameSchedule.stadiumId,
						Expressions.nullExpression(String.class), // homeTeam DisplayName
						Expressions.nullExpression(String.class), // awayTeamName DisplayName
						Expressions.nullExpression(String.class), // stadiumLocation
						gameStatus.gameStatus,
						gameStatus.homeTeamScore,
						gameStatus.awayTeamScore,
						gameStatus.gameResult,
						ticketingStatus.status,
						ticketingStatus.ticketingOpenedAt,
						ticketingStatus.ticketingEndAt,
						Expressions.constant(0L)
					)
				)
				.from(gameSchedule)
				.leftJoin(gameStatus).on(gameStatus.gameSchedule.eq(gameSchedule))
				.leftJoin(ticketingStatus).on(ticketingStatus.gameSchedule.eq(gameSchedule))
				.where(gameSchedule.id.eq(gameId))
				.fetchOne()
		);
	}

	private BooleanExpression teamIdEq(QGameScheduleEntity game, UUID teamId) {
		if (teamId == null)
			return null;
		return game.homeTeamId.eq(teamId).or(game.awayTeamId.eq(teamId));
	}

	private BooleanExpression dateFilter(
		QGameScheduleEntity game, GameScheduleSearchCondition condition, LocalDateTime now
	) {
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
		int currentYear = now.getYear();
		LocalDateTime startOfYear = LocalDateTime.of(
			currentYear, 1, 1, 0, 0
		);
		return game.startAt.between(startOfYear, startOfYear.plusYears(1).minusNanos(1));
	}

	private BooleanExpression isAnyTeamInvolved(UUID homeId, UUID awayId) {
		return gameSchedule.homeTeamId.in(homeId, awayId)
			.or(gameSchedule.awayTeamId.in(homeId, awayId));
	}
}
