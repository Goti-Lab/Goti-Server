package com.goti.domain.entity.team;

import static lombok.AccessLevel.*;

import com.goti.constants.TeamCode;
import com.goti.domain.base.ModificationTimestampEntity;

import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import org.springframework.util.StringUtils;

import java.time.LocalDate;

@Getter
@Entity
@Table(
	name = "baseball_teams",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_baseball_team_team_code", columnNames = {"team_code"})
	})
@NoArgsConstructor(access = PROTECTED)
public class BaseballTeamEntity extends ModificationTimestampEntity {
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TeamCode teamCode;

	@Column(nullable = false)
	private String teamName;

	@Column(nullable = false)
	private String teamNameEn;

	@Column(nullable = false)
	private String sponsor;

	@Column(nullable = false)
	private String homeGround;

	@Column(nullable = false)
	private Integer foundedYear;

	@Column(nullable = false)
	private String officeAddress;

	@Column(nullable = false)
	private String zipCode;

	@Column(nullable = false)
	private String siteAddress;

	@Column(nullable = false)
	private String owner;

	@Column
	private String ownerAgency;

	@Column
	private String ceo;

	@Column(nullable = false)
	private String generalManager;

	@Column(nullable = false)
	private String director;

	@Column
	private String logoUrl;

	private BaseballTeamEntity(
		TeamCode teamCode,
		String teamName,
		String teamNameEn,
		String sponsor,
		String homeGround,
		Integer foundedYear,
		String officeAddress,
		String zipCode,
		String siteAddress,
		String owner,
		String ownerAgency,
		String ceo,
		String generalManager,
		String director,
		String logoUrl
	) {
		this.teamCode = teamCode;
		this.teamName = teamName;
		this.teamNameEn = teamNameEn;
		this.sponsor = sponsor;
		this.homeGround = homeGround;
		this.foundedYear = foundedYear;
		this.officeAddress = officeAddress;
		this.zipCode = zipCode;
		this.siteAddress = siteAddress;
		this.owner = owner;
		this.ownerAgency = ownerAgency;
		this.ceo = ceo;
		this.generalManager = generalManager;
		this.director = director;
		this.logoUrl = logoUrl;
	}

	public static BaseballTeamEntity create(
		TeamCode teamCode,
		String teamName,
		String teamNameEn,
		String sponsor,
		String homeGround,
		Integer foundedYear,
		String officeAddress,
		String zipCode,
		String siteAddress,
		String owner,
		String generalManager,
		String director,
		String ownerAgency,
		String ceo,
		String logoUrl
	) {

		validate(
			teamCode, teamName, teamNameEn, sponsor,
			homeGround, foundedYear, officeAddress, zipCode,
			siteAddress, owner, generalManager, director
		);

		return new BaseballTeamEntity(
			teamCode, teamName, teamNameEn, sponsor, homeGround, foundedYear,
			officeAddress, zipCode, siteAddress, owner,
			ownerAgency, ceo, generalManager, director, logoUrl
		);

	}

	private static void validate(
		TeamCode teamCode,
		String teamName,
		String teamNameEn,
		String sponsor,
		String homeGround,
		Integer foundedYear,
		String officeAddress,
		String zipCode,
		String siteAddress,
		String owner,
		String generalManager,
		String director
	) {

		Preconditions.domainValidate(
			teamCode != null,
			"구단(팀)코드는 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(teamName),
			"구단(팀)명은 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(teamNameEn),
			"구단(팀) 영문명은 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(sponsor),
			"스폰서는 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(homeGround),
			"연고지는 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			foundedYear != null && foundedYear <= LocalDate.now().getYear(),
			"창단년도는 비어있거나 현재연도와 같거나 미래일 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(officeAddress),
			"구단 사무실 도로명주소는 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(zipCode),
			"구단 사무실 우편주소는 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(siteAddress),
			"구단 사이트 주소는 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(owner), "구단주명은 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(generalManager), "단장명은 비어있을 수 없습니다."
		);

		Preconditions.domainValidate(
			StringUtils.hasText(director), "감독명은 비어있을 수 없습니다."
		);

	}
}