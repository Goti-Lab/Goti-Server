package com.goti.domain.entity.resale;

import static lombok.AccessLevel.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "resale_restrictions",
	indexes = {
		@Index(name = "unique_idx_user_id", columnList = "user_id", unique = true)
	})
@NoArgsConstructor(access = PROTECTED)
public class ResaleRestrictionEntity extends ModificationTimestampEntity {

	private static final int MAX_DAILY_SELL_COUNT = 10;
	private static final int MAX_DAILY_BUY_COUNT = 10;
	private static final int MAX_DAILY_CANCEL_COUNT = 10;
	private static final int MAX_GAME_SELL_COUNT = 5;
	private static final int MAX_GAME_BUY_COUNT = 3;
	private static final int MAX_GAME_CANCEL_COUNT = 3;

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

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "game_sell_counts", columnDefinition = "jsonb")
	private Map<UUID, Integer> gameSellCounts = new HashMap<>();

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "game_buy_counts", columnDefinition = "jsonb")
	private Map<UUID, Integer> gameBuyCounts = new HashMap<>();

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "game_cancel_counts", columnDefinition = "jsonb")
	private Map<UUID, Integer> gameCancelCounts = new HashMap<>();

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

	private static void validate(
		UUID userId
	) {
		Preconditions.domainValidate(userId != null, "유저 ID는 비어 있을 수 없습니다.");
	}

	public int getGameSellCount(UUID gameId) {
		return gameSellCounts.getOrDefault(gameId, 0);
	}

	public int getGameBuyCount(UUID gameId) {
		return gameBuyCounts.getOrDefault(gameId, 0);
	}

	public int getGameCancelCount(UUID gameId) {
		return gameCancelCounts.getOrDefault(gameId, 0);
	}

	public boolean isResaleBlocked() {
		if (resaleBlockedUntil == null) {
			return false;
		}
		LocalDate today = LocalDate.now();
		return today.isBefore(resaleBlockedUntil) || today.isEqual(resaleBlockedUntil);
	}

	public boolean isTodayAction(LocalDateTime actionTime) {
		if (actionTime == null) {
			return false;
		}
		return actionTime.toLocalDate().isEqual(LocalDate.now());
	}

	public void incrementSellCount(UUID gameId) {
		if (!isTodayAction(lastSellAt)) {
			this.dailySellCount = 0;
		} else {
			this.dailySellCount++;
		}
		this.lastSellAt = LocalDateTime.now();
		getGameSellCounts().merge(gameId, 1, Integer::sum);
	}

	public void incrementBuyCount(UUID gameId) {
		if (!isTodayAction(lastBuyAt)) {
			this.dailyBuyCount = 0;
		} else {
			this.dailyBuyCount++;
		}
		this.lastBuyAt = LocalDateTime.now();
		getGameBuyCounts().merge(gameId, 1, Integer::sum);
	}

	public void incrementCancelCount(UUID gameId) {
		if (!isTodayAction(lastCancelAt)) {
			this.dailyCancelCount = 0;
		} else {
			this.dailyCancelCount++;
		}
		this.lastCancelAt = LocalDateTime.now();
		getGameCancelCounts().merge(gameId, 1, Integer::sum);
	}

	public void blockResale(LocalDate until) {
		Preconditions.domainValidate(
			until != null && until.isAfter(LocalDate.now()),
			"차단 날짜는 미래여야 합니다."
		);
		this.resaleBlockedUntil = until;
	}

	public void unblockResale() {
		this.resaleBlockedUntil = null;
	}
}
