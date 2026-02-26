package com.goti.domain.entity.resale;

import static lombok.AccessLevel.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "resale_restrictions")
@NoArgsConstructor(access = PROTECTED)
public class ResaleRestrictionEntity extends ModificationTimestampEntity {
	@Column(nullable = false)
	private UUID userId;

	@Column(nullable = false)
	private Integer dailyBuyCount;

	@Column(nullable = false)
	private Integer dailySellCount;

	@Column(nullable = false)
	private Integer dailyCancelCount;

	private LocalDateTime lastBuyAt;

	private LocalDateTime lastSellAt;

	private LocalDateTime lastCancelAt;

	private LocalDate resaleBlockedUntil;

	private ResaleRestrictionEntity(
		UUID userId
	) {
		this.userId = userId;
		this.dailyBuyCount = 0;
		this.dailySellCount = 0;
		this.dailyCancelCount = 0;
		this.lastBuyAt = null;
		this.lastSellAt = null;
		this.lastCancelAt = null;
		this.resaleBlockedUntil = null;
	}

	public static ResaleRestrictionEntity create(
		UUID userId
	) {
		validate(userId);

		return new ResaleRestrictionEntity(
			userId
		);
	}

	public void recordBuy() {
		this.dailyBuyCount++;
		this.lastBuyAt = LocalDateTime.now();
	}

	public void recordSell() {
		this.dailySellCount++;
		this.lastSellAt = LocalDateTime.now();
	}

	public void recordCancel() {
		this.dailyCancelCount++;
		this.lastCancelAt = LocalDateTime.now();
	}

	public void resetDailyCounts() {
		this.dailyBuyCount = 0;
		this.dailySellCount = 0;
		this.dailyCancelCount = 0;
	}

	/*
	 * TODO : Count 이전에 해당 기능이 막혀있는지에 대한 validate 추가, 그리고 리셋 관련 로직, 상태 검증 로직 (최대횟수미만인지) 등
	 * */

	private static void validate(
		UUID userId
	) {
		Preconditions.domainValidate(userId != null, "유저 ID는 비어 있을 수 없습니다.");
	}

}
