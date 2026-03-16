package com.goti.domain.entity.stadium;

import com.goti.constants.StadiumType;
import com.goti.domain.base.ModificationTimestampEntity;

import com.goti.domain.entity.team.BaseballTeamEntity;

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

import static lombok.AccessLevel.PROTECTED;

@Getter
@Entity
@Table(name = "home_stadiums")
@NoArgsConstructor(access = PROTECTED)
public class HomeStadiumEntity extends ModificationTimestampEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "baseball_team_id", nullable = false)
	private BaseballTeamEntity baseballTeam;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "stadium_id", nullable = false)
	private StadiumEntity stadium;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private StadiumType type;

	private HomeStadiumEntity(
		BaseballTeamEntity baseballTeam,
		StadiumEntity stadium,
		StadiumType type
	) {
		this.baseballTeam = baseballTeam;
		this.stadium = stadium;
		this.type = type;
	}

	public static HomeStadiumEntity create(
		final BaseballTeamEntity baseballTeam,
		final StadiumEntity stadium,
		final StadiumType type
	) {
		validate(baseballTeam, stadium, type);
		return new HomeStadiumEntity(
			baseballTeam, stadium, type
		);
	}

	private static void validate(
		BaseballTeamEntity baseballTeam,
		StadiumEntity stadium,
		StadiumType type
	) {
		Preconditions.domainValidate(
			baseballTeam != null,
			"야구구단 정보는 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			stadium != null,
			"구장 정보는 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			type != null,
			"구장 타입은 비어있을 수 없습니다."
		);
	}

}
