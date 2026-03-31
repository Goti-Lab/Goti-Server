package com.goti.resale.domain.entity.resale;

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
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "resale_restrictions",
	indexes = {
		@Index(name = "uk_user_id", columnList = "user_id", unique = true)
	})
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

	private LocalDateTime resaleBlockedUntil;
	private LocalDateTime buyBlockedUntil;
	private LocalDateTime cancelBlockedUntil;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "game_sell_counts", columnDefinition = "jsonb")
	private Map<UUID, Integer> gameSellCounts = new HashMap<>();

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "game_buy_counts", columnDefinition = "jsonb")
	private Map<UUID, Integer> gameBuyCounts = new HashMap<>();

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "game_cancel_counts", columnDefinition = "jsonb")
	private Map<UUID, Integer> gameCancelCounts = new HashMap<>();

	@Version
	private Long version;

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
		this.buyBlockedUntil = null;
		this.cancelBlockedUntil = null;
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

	public boolean isTodayAction(LocalDateTime actionTime) {
		if (actionTime == null) {
			return false;
		}
		return actionTime.toLocalDate().isEqual(LocalDate.now());
	}

	public boolean isResaleBlocked() {
		if (resaleBlockedUntil == null) {
			return false;
		}
		return LocalDateTime.now().isBefore(resaleBlockedUntil);
	}

	public boolean isBuyBlocked() {
		if (buyBlockedUntil == null) {
			return false;
		}
		return LocalDateTime.now().isBefore(buyBlockedUntil);
	}

	public boolean isCancelBlocked() {
		if (cancelBlockedUntil == null) {
			return false;
		}
		return LocalDateTime.now().isBefore(cancelBlockedUntil);
	}

	public void incrementSellCount(UUID gameId) {
		LocalDate today = LocalDate.now();

		if (lastSellAt == null || !lastSellAt.toLocalDate().isEqual(today)) {
			this.dailySellCount = 1;
		} else {
			this.dailySellCount++;
		}

		this.lastSellAt = LocalDateTime.now();
		gameSellCounts.merge(gameId, 1, Integer::sum);
	}

	public void incrementBuyCount(UUID gameId) {
		LocalDate today = LocalDate.now();

		if (lastBuyAt == null || !lastBuyAt.toLocalDate().isEqual(today)) {
			this.dailyBuyCount = 1;
		} else {
			this.dailyBuyCount++;
		}

		this.lastBuyAt = LocalDateTime.now();
		gameBuyCounts.merge(gameId, 1, Integer::sum);
	}

	public void incrementCancelCount(UUID gameId) {
		LocalDate today = LocalDate.now();

		if (lastCancelAt == null || !lastCancelAt.toLocalDate().isEqual(today)) {
			this.dailyCancelCount = 1;
		} else {
			this.dailyCancelCount++;
		}

		this.lastCancelAt = LocalDateTime.now();
		gameCancelCounts.merge(gameId, 1, Integer::sum);
	}

	public void blockResale() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		this.resaleBlockedUntil = tomorrow.atStartOfDay();
	}

	public void unblockResale() {
		this.resaleBlockedUntil = null;
	}

	public void blockBuy() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		this.buyBlockedUntil = tomorrow.atStartOfDay();
	}

	public void unblockBuy() {
		this.buyBlockedUntil = null;
	}

	public void blockCancel() {
		LocalDate tomorrow = LocalDate.now().plusDays(1);
		this.cancelBlockedUntil = tomorrow.atStartOfDay();
	}

	public void unblockCancel() {
		this.cancelBlockedUntil = null;
	}
}
