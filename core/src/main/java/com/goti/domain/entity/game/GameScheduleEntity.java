package com.goti.domain.entity.game;

import com.goti.constants.LeagueType;
import com.goti.domain.base.ModificationTimestampEntity;

import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import static lombok.AccessLevel.*;

@Getter
@Entity
@Table(name = "game_schedules")
@NoArgsConstructor(access = PROTECTED)
public class GameScheduleEntity extends ModificationTimestampEntity {
	@Column(nullable = false)
	private UUID homeTeamId;

	@Column(nullable = false)
	private UUID awayTeamId;

	@Column(nullable = false)
	private UUID stadiumId;

	@Column(nullable = false)
	private LocalDateTime startAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private LeagueType leagueType;

	@OneToOne(mappedBy = "gameSchedule", fetch = FetchType.LAZY)
	private GameStatusEntity gameStatus;

	@OneToOne(mappedBy = "gameSchedule", fetch = FetchType.LAZY)
	private GameTicketingStatusEntity ticketingStatus;

	public void initGameStatus(GameStatusEntity gameStatus) {
		this.gameStatus = gameStatus;
	}

	public void initTicketingStatus(GameTicketingStatusEntity gameTicketingStatus) {
		this.ticketingStatus = gameTicketingStatus;
	}

	private GameScheduleEntity(
		UUID homeTeamId,
		UUID awayTeamId,
		UUID stadiumId,
		LocalDateTime startAt,
		LeagueType leagueType
	) {
		this.homeTeamId = homeTeamId;
		this.awayTeamId = awayTeamId;
		this.stadiumId = stadiumId;
		this.startAt = startAt;
		this.leagueType = leagueType;
	}

	public static GameScheduleEntity create(
		UUID homeTeamId,
		UUID awayTeamId,
		UUID stadiumId,
		LocalDateTime startAt,
		LeagueType leagueType
	) {

		validate(homeTeamId, awayTeamId, stadiumId, leagueType);
		validateStartAt(startAt);

		return new GameScheduleEntity(
			homeTeamId,
			awayTeamId,
			stadiumId,
			startAt,
			leagueType
		);
	}

	private static void validate(
		UUID homeTeamId,
		UUID awayTeamId,
		UUID stadiumId,
		LeagueType leagueType
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
			leagueType != null,
			"경기 타입은 필수입니다."
		);

	}

	private static void validateStartAt(LocalDateTime startAt) {
		Preconditions.domainValidate(
			startAt != null,
			"경기 시작 시간은 필수입니다."
		);
		LocalDateTime now = LocalDateTime.now();
		Preconditions.domainValidate(
			startAt.isAfter(now),
			"과거 시점의 경기 일정은 생성할 수 없습니다."
		);

		Preconditions.domainValidate(
			!startAt.toLocalDate().isEqual(now.toLocalDate()),
			"경기 당일에는 일정을 등록할 수 없습니다"
		);
	}
}
