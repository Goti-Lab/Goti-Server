package com.goti.domain.entity.game;

import com.goti.constants.LeagueType;
import com.goti.constants.ReservationAvailableStatus;
import com.goti.domain.base.ModificationTimestampEntity;

import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static lombok.AccessLevel.*;

@Getter
@Entity
@Table(name = "baseball_games")
@NoArgsConstructor(access = PROTECTED)
public class BaseballGameEntity extends ModificationTimestampEntity {
	@Column(nullable = false)
	private UUID homeTeamId;

	@Column(nullable = false)
	private UUID awayTeamId;

	@Column(nullable = false)
	private UUID stadiumId;

	@Column(nullable = false)
	private LocalDate playDate;

	@Column(nullable = false)
	private LocalTime startAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LeagueType leagueType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ReservationAvailableStatus reservationAvailableStatus;

	@Column(nullable = false)
	private LocalDateTime reservationOpenedAt;

	@Column(nullable = false)
	private LocalDateTime reservationClosedAt;

	private BaseballGameEntity(
		UUID homeTeamId,
		UUID awayTeamId,
		UUID stadiumId,
		LocalDate playDate,
		LocalTime startAt,
		LocalDateTime reservationOpenedAt,
		LocalDateTime reservationClosedAt
	) {
		this.homeTeamId = homeTeamId;
		this.awayTeamId = awayTeamId;
		this.stadiumId = stadiumId;
		this.playDate = playDate;
		this.startAt = startAt;
		this.leagueType = LeagueType.REGULAR;
		this.reservationAvailableStatus = ReservationAvailableStatus.PENDING;
		this.reservationOpenedAt = reservationOpenedAt;
		this.reservationClosedAt = reservationClosedAt;
	}

	public static BaseballGameEntity create(
		UUID homeTeamId,
		UUID awayTeamId,
		UUID stadiumId,
		LocalDate playDate,
		LocalTime startAt,
		LocalDateTime reservationOpenedAt,
		LocalDateTime reservationClosedAt
	) {

		validate(
			homeTeamId, awayTeamId, stadiumId,
			playDate, startAt,
			reservationOpenedAt, reservationClosedAt
		);

		return new BaseballGameEntity(
			homeTeamId,
			awayTeamId,
			stadiumId,
			playDate,
			startAt,
			reservationOpenedAt,
			reservationClosedAt
		);
	}

	private static void validate(
		UUID homeTeamId,
		UUID awayTeamId,
		UUID stadiumId,
		LocalDate playDate,
		LocalTime startAt,
		LocalDateTime reservationOpenedAt,
		LocalDateTime reservationClosedAt
	) {

		Preconditions.domainValidate(
			homeTeamId != null,
			"홈팀 ID는 필수입니다."
		)		;
		Preconditions.domainValidate(
			awayTeamId != null,
			"원정팀 ID는 필수입니다."
		);
		Preconditions.domainValidate(
			stadiumId != null,
			"구장 ID는 필수입니다."
		);

		Preconditions.domainValidate(
			!homeTeamId.equals(awayTeamId),
			"홈팀과 원정팀은 같을 수 없습니다."
		);

		Preconditions.domainValidate(
			playDate != null,
			"경기 날짜는 필수입니다."
		);
		Preconditions.domainValidate(
			startAt != null,
			"경기 시작 시간은 필수입니다."
		);

		Preconditions.domainValidate(
			reservationOpenedAt != null,
			"예매 오픈 일시는 필수입니다."
		);
		Preconditions.domainValidate(
			reservationClosedAt != null,
			"예매 종료 일시는 필수입니다."
		);

		Preconditions.domainValidate(
			!reservationOpenedAt.isAfter(reservationClosedAt),
			"예매 오픈 일시는 종료 일시보다 늦을 수 없습니다."
		);
	}
}
