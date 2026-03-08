package com.goti.domain.entity.game;

import static lombok.AccessLevel.*;

import com.goti.constants.GameResult;
import com.goti.constants.GameStatus;
import com.goti.domain.base.ModificationTimestampEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "baseball_game_statuses")
@NoArgsConstructor(access = PROTECTED)
public class BaseballGameStatusEntity extends ModificationTimestampEntity {

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

	private BaseballGameStatusEntity(
		GameScheduleEntity gameSchedule,
		GameStatus gameStatus,
		GameResult gameResult
	) {
		this.gameSchedule = gameSchedule;
		this.gameStatus = gameStatus;
		this.homeTeamScore = 0;
		this.awayTeamScore = 0;
		this.gameResult = gameResult;
	}

	// TODO: 추후 점수 변경 시 메서드로 변경

	public static BaseballGameStatusEntity init(GameScheduleEntity game) {
		return new BaseballGameStatusEntity(game, GameStatus.SCHEDULED, GameResult.PENDING);
	}
}
