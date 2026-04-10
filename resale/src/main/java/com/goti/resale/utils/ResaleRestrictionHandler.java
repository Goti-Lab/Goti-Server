package com.goti.resale.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;
import com.goti.resale.domain.entity.resale.ResaleRestrictionEntity;

@Component
public class ResaleRestrictionHandler {
	private static final int MAX_DAILY_SELL_COUNT = 10;
	private static final int MAX_DAILY_BUY_COUNT = 10;
	private static final int MAX_DAILY_CANCEL_COUNT = 10;
	private static final int MAX_GAME_SELL_COUNT = 5;
	private static final int MAX_GAME_BUY_COUNT = 3;
	private static final int MAX_GAME_CANCEL_COUNT = 3;
	private static final int MAX_GAME_POSSESSION_COUNT = 4;
	private static final int RE_LISTING_MIN_HOURS = 6;

	public void validateCanSell(ResaleRestrictionEntity restriction, UUID gameId) {
		Preconditions.validate(!restriction.isResaleBlocked(), ErrorCode.RESALE_BLOCKED);

		if (restriction.isTodayAction(restriction.getLastSellAt())) {
			Preconditions.validate(
				restriction.getDailySellCount() < MAX_DAILY_SELL_COUNT,
				ErrorCode.DAILY_SELL_LIMIT_EXCEEDED, String.valueOf(MAX_DAILY_SELL_COUNT)
			);
		}

		Preconditions.validate(
			restriction.getGameSellCount(gameId) < MAX_GAME_SELL_COUNT,
			ErrorCode.GAME_SELL_LIMIT_EXCEEDED, String.valueOf(MAX_GAME_SELL_COUNT)
		);
	}

	public void validateCanBuy(ResaleRestrictionEntity restriction, UUID gameId) {
		Preconditions.validate(!restriction.isBuyBlocked(), ErrorCode.RESALE_BLOCKED);

		if (restriction.isTodayAction(restriction.getLastBuyAt())) {
			Preconditions.validate(
				restriction.getDailyBuyCount() < MAX_DAILY_BUY_COUNT,
				ErrorCode.DAILY_BUY_LIMIT_EXCEEDED, String.valueOf(MAX_DAILY_BUY_COUNT)
			);
		}

		Preconditions.validate(
			restriction.getGameBuyCount(gameId) < MAX_GAME_BUY_COUNT,
			ErrorCode.GAME_BUY_LIMIT_EXCEEDED, String.valueOf(MAX_GAME_BUY_COUNT)
		);
	}

	public void validateCanCancel(ResaleRestrictionEntity restriction, UUID gameId) {
		Preconditions.validate(!restriction.isCancelBlocked(), ErrorCode.RESALE_BLOCKED);

		if (restriction.isTodayAction(restriction.getLastCancelAt())) {
			Preconditions.validate(
				restriction.getDailyCancelCount() < MAX_DAILY_CANCEL_COUNT,
				ErrorCode.DAILY_CANCEL_LIMIT_EXCEEDED, String.valueOf(MAX_DAILY_CANCEL_COUNT)
			);
		}

		Preconditions.validate(
			restriction.getGameCancelCount(gameId) < MAX_GAME_CANCEL_COUNT,
			ErrorCode.GAME_CANCEL_LIMIT_EXCEEDED, String.valueOf(MAX_GAME_CANCEL_COUNT)
		);
	}

	public void validatePossessionLimit(int currentOwnedCount, int pendingCount, int requestCount) {
		Preconditions.validate(
			currentOwnedCount + pendingCount + requestCount <= MAX_GAME_POSSESSION_COUNT,
			ErrorCode.GAME_POSSESSION_LIMIT_EXCEEDED, String.valueOf(MAX_GAME_POSSESSION_COUNT)
		);
	}

	public void validateReListingLimit(UUID transactionId, LocalDateTime createdAt) {
		if (transactionId != null && createdAt != null) {
			long hoursPassed = Duration.between(createdAt, LocalDateTime.now(ZoneId.of("Asia/Seoul"))).toHours();
			Preconditions.validate(
				hoursPassed >= RE_LISTING_MIN_HOURS,
				ErrorCode.RE_LISTING_LIMIT_EXCEEDED, String.valueOf(RE_LISTING_MIN_HOURS)
			);
		}
	}

	public void handleAfterSell(ResaleRestrictionEntity restriction, UUID gameId) {
		restriction.incrementSellCount(gameId);

		if (restriction.getDailySellCount() >= MAX_DAILY_SELL_COUNT) {
			restriction.blockResale();
		}
	}

	public void handleAfterBuy(ResaleRestrictionEntity restriction, UUID gameId) {
		restriction.incrementBuyCount(gameId);

		if (restriction.getDailyBuyCount() >= MAX_DAILY_BUY_COUNT) {
			restriction.blockBuy();
		}
	}

	public void handleAfterCancel(ResaleRestrictionEntity restriction, UUID gameId) {
		restriction.incrementCancelCount(gameId);

		if (restriction.getDailyCancelCount() >= MAX_DAILY_CANCEL_COUNT) {
			restriction.blockCancel();
		}
	}
}