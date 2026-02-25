package com.goti.domain.entity.baseball;

import static lombok.AccessLevel.*;

import com.goti.constants.TeamCode;
import com.goti.domain.base.ModificationTimestampEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
		return new BaseballTeamEntity(
			teamCode, teamName, teamNameEn, sponsor, homeGround, foundedYear,
			officeAddress, zipCode, siteAddress, owner,
			ownerAgency, ceo, generalManager, director, logoUrl
		);
	}
}