package com.goti.ticketing.game.repository.gameschedule;

import com.goti.stadium.domain.entity.stadium.QStadiumEntity;
import com.goti.stadium.domain.entity.team.QBaseballTeamEntity;
import com.goti.ticketing.domain.entity.game.QGameScheduleEntity;
import com.goti.ticketing.domain.entity.game.QGameStatusEntity;
import com.goti.ticketing.domain.entity.game.QGameTicketingStatusEntity;
import com.goti.ticketing.game.dto.request.GameScheduleSearchCondition;
import com.goti.ticketing.game.dto.response.GameScheduleSearchResponse;
import com.goti.ticketing.game.dto.response.QGameScheduleSearchResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static com.goti.ticketing.domain.entity.game.QGameScheduleEntity.gameScheduleEntity;

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
	public List<GameScheduleSearchResponse> searchSchedules(GameScheduleSearchCondition condition) {

		QBaseballTeamEntity homeTeam = new QBaseballTeamEntity("homeTeam");
		QBaseballTeamEntity awayTeam = new QBaseballTeamEntity("awayTeam");
		QStadiumEntity stadium = QStadiumEntity.stadiumEntity;

		return jpaQueryFactory
			.select(
				new QGameScheduleSearchResponse(
					gameSchedule.id,
					gameSchedule.startAt,
					gameSchedule.leagueType,
					gameSchedule.homeTeamId,
					gameSchedule.awayTeamId,
					gameSchedule.stadiumId,
					homeTeam.displayName,
					awayTeam.displayName,
					stadium.location,
					gameStatus.gameStatus,
					gameStatus.homeTeamScore,
					gameStatus.awayTeamScore,
					gameStatus.gameResult,
					ticketingStatus.status,
					ticketingStatus.ticketingOpenedAt,
					ticketingStatus.ticketingEndAt
				)
			)
			.from(gameSchedule)
			.leftJoin(gameStatus).on(gameStatus.gameSchedule.eq(gameSchedule))
			.leftJoin(ticketingStatus).on(ticketingStatus.gameSchedule.eq(gameSchedule))
			.leftJoin(homeTeam).on(homeTeam.id.eq(gameSchedule.homeTeamId))
			.leftJoin(awayTeam).on(awayTeam.id.eq(gameSchedule.awayTeamId))
			.leftJoin(stadium).on(stadium.id.eq(gameSchedule.stadiumId))
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
