package com.goti.stadium.domain.entity.team;

import static lombok.AccessLevel.*;

import java.time.Year;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "baseball_winner_histories",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_winner_history_team_year", columnNames = {"baseball_team_id", "winning_year"})
	})
@NoArgsConstructor(access = PROTECTED)
public class BaseballWinnerHistoryEntity extends ModificationTimestampEntity {
	// Todo: 관리자 및 야구매니저 기획 확정 시 createdBy 작성 예정

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "baseball_team_id", nullable = false)
	private BaseballTeamEntity baseballTeam;

	@Column(nullable = false)
	private Integer winningYear;

	private BaseballWinnerHistoryEntity(
		BaseballTeamEntity baseballTeam,
		Integer winningYear
	) {
		this.baseballTeam = baseballTeam;
		this.winningYear = winningYear;
	}

	public static BaseballWinnerHistoryEntity create(
		BaseballTeamEntity baseballTeam,
		Integer winningYear
	) {
		validate(baseballTeam, winningYear);
		return new BaseballWinnerHistoryEntity(baseballTeam, winningYear);
	}

	private static void validate(
		BaseballTeamEntity baseballTeam,
		Integer winningYear
	) {
		Preconditions.domainValidate(
			baseballTeam != null,
			"우승 이력은 구단 정보가 반드시 필요합니다."
		);

		Preconditions.domainValidate(
			winningYear != null,
			"우승 연도는 비어 있을 수 없습니다."
		);

		Preconditions.domainValidate(
			winningYear >= 1900 && winningYear <= Year.now().getValue(),
			"우승 연도는 올바른 범위의 값이어야 합니다."
		);
	}
}
