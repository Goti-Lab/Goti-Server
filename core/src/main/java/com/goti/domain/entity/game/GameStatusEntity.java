package com.goti.domain.entity.game;

import static lombok.AccessLevel.*;

import com.goti.constants.GameResult;
import com.goti.constants.GameStatus;
import com.goti.domain.base.ModificationTimestampEntity;

import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "game_statuses")
@NoArgsConstructor(access = PROTECTED)
public class GameStatusEntity extends ModificationTimestampEntity {

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_schedule_id", nullable = false)
	private GameScheduleEntity gameSchedule;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GameStatus gameStatus;

	@Column(nullable = false)
	private Integer homeTeamScore;

	@Column(nullable = false)
	private Integer awayTeamScore;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private GameResult gameResult;

	private GameStatusEntity(GameScheduleEntity gameSchedule) {
		this.gameSchedule = gameSchedule;
		this.gameStatus = GameStatus.SCHEDULED;
		this.homeTeamScore = 0;
		this.awayTeamScore = 0;
		this.gameResult = GameResult.NONE;
	}

	public static GameStatusEntity create(GameScheduleEntity gameSchedule) {
		validate(gameSchedule);
		return new GameStatusEntity(gameSchedule);
	}

	private static void validate(GameScheduleEntity gameSchedule) {
		Preconditions.domainValidate(
			gameSchedule != null,
			"게임일정 값은 비어있을 수 없습니다."
		);
	}
}
