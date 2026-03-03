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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "baseball_game_statuses")
@NoArgsConstructor(access = PROTECTED)
public class BaseballGameStatusEntity extends ModificationTimestampEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "baseball_game_id", nullable = false)
	private BaseballGameEntity baseballGameId;

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
		BaseballGameEntity baseballGameId,
		GameStatus gameStatus,
		Integer homeTeamScore,
		Integer awayTeamScore,
		GameResult gameResult
	) {
		this.baseballGameId = baseballGameId;
		this.gameStatus = gameStatus;
		this.homeTeamScore = homeTeamScore;
		this.awayTeamScore = awayTeamScore;
		this.gameResult = gameResult;
	}

	public static BaseballGameStatusEntity create(
		BaseballGameEntity baseballGameId,
		GameStatus gameStatus,
		Integer homeTeamScore,
		Integer awayTeamScore,
		GameResult gameResult
	) {

		validate(gameStatus);

		return new BaseballGameStatusEntity(
			baseballGameId,
			gameStatus == null ? GameStatus.SCHEDULED : gameStatus,
			0,
			0,
			gameResult == null ? GameResult.PENDING : gameResult
		);
	}

	private static void validate(
		GameStatus gameStatus
	) {
		Preconditions.domainValidate(
			gameStatus != null,
			"경기 상태는 필수입니다."
		);
	}
}
