package com.goti.domain.entity.resale;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.constants.ResaleAvailableStatus;
import com.goti.constants.ResaleListingStatus;
import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "resale_listings",
	indexes = {
		@Index(name = "unique_idx_ticket_id", columnList = "ticket_id", unique = true),
		@Index(name = "idx_seller_id", columnList = "seller_id"),
		@Index(name = "idx_game_id", columnList = "game_id")
	})
@NoArgsConstructor(access = PROTECTED)
public class ResaleListingEntity extends ModificationTimestampEntity {
	@Column(nullable = false)
	private UUID ticketId;

	@Column(nullable = false)
	private UUID sellerId;

	@Column(nullable = false)
	private UUID gameId;

	@Column(nullable = false)
	private UUID seatId;

	@Column(nullable = false)
	private UUID gradeId;

	@Column(nullable = false)
	private String seatInfo;

	@Column(nullable = false)
	private Integer dailyBasePrice;

	@Column(nullable = false)
	private Integer listingPrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ResaleListingStatus listingStatus;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ResaleAvailableStatus availableStatus;

	private Integer lastTransactionPrice;

	@Column(nullable = false)
	private LocalDateTime listedAt;

	private LocalDateTime soldAt;

	private LocalDateTime canceledAt;

	public ResaleListingEntity(
		UUID ticketId,
		UUID sellerId,
		UUID gameId,
		UUID seatId,
		UUID gradeId,
		String seatInfo,
		Integer dailyBasePrice,
		Integer listingPrice
	) {
		this.ticketId = ticketId;
		this.sellerId = sellerId;
		this.gameId = gameId;
		this.seatId = seatId;
		this.gradeId = gradeId;
		this.seatInfo = seatInfo;
		this.dailyBasePrice = dailyBasePrice;
		this.listingPrice = listingPrice;
		this.listingStatus = ResaleListingStatus.LISTING;
		this.availableStatus = ResaleAvailableStatus.ENABLED;
		this.lastTransactionPrice = null;
		this.listedAt = LocalDateTime.now();
		this.soldAt = null;
		this.canceledAt = null;
	}

	public static ResaleListingEntity create(
		UUID ticketId,
		UUID sellerId,
		UUID gameId,
		UUID seatId,
		UUID gradeId,
		String seatInfo,
		Integer dailyBasePrice,
		Integer listingPrice
	) {
		validate(ticketId, sellerId, gameId, seatId, gradeId, seatInfo, dailyBasePrice, listingPrice);

		return new ResaleListingEntity(
			ticketId,
			sellerId,
			gameId,
			seatId,
			gradeId,
			seatInfo,
			dailyBasePrice,
			listingPrice
		);
	}

	private static void validate(
		UUID ticketId,
		UUID sellerId,
		UUID gameId,
		UUID seatId,
		UUID gradeId,
		String seatInfo,
		Integer dailyBasePrice,
		Integer listingPrice
	) {
		Preconditions.domainValidate(ticketId != null, "티켓 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(sellerId != null, "판매자 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(gameId != null, "경기 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(seatId != null, "좌석 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(gradeId != null, "등급 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(seatInfo != null, "좌석 정보는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(dailyBasePrice != null && dailyBasePrice >= 0, "일일 기준가는 0 이상이어야 합니다.");
		Preconditions.domainValidate(listingPrice != null && listingPrice >= 0, "판매가는 0 이상이어야 합니다.");
	}

	public void cancel() {
		Preconditions.domainValidate(isCancelable(), "취소할 수 없는 상태입니다.");

		this.listingStatus = ResaleListingStatus.CANCELED;
		this.availableStatus = ResaleAvailableStatus.DISABLED;
		this.canceledAt = LocalDateTime.now();
	}

	public void hold() {
		Preconditions.domainValidate(isPurchasable(), "구매할 수 없는 상태입니다.");

		this.listingStatus = ResaleListingStatus.HOLD;
	}

	public void releaseHold() {
		Preconditions.domainValidate(listingStatus == ResaleListingStatus.HOLD, "HOLD상태에만 가능합니다.");

		this.listingStatus = ResaleListingStatus.LISTING;
	}

	public void SoldOut(Integer transactionPrice) {
		Preconditions.domainValidate(
			this.listingStatus == ResaleListingStatus.HOLD,
			"HOLD상태에서만 판매할 수 있습니다."
		);

		this.listingStatus = ResaleListingStatus.SOLD;
		this.availableStatus = ResaleAvailableStatus.DISABLED;
		this.lastTransactionPrice = transactionPrice;
		this.soldAt = LocalDateTime.now();
	}

	public void cancelByGameStart() {
		if (this.listingStatus == ResaleListingStatus.LISTING
			|| this.listingStatus == ResaleListingStatus.HOLD) {
			this.listingStatus = ResaleListingStatus.CANCELED;
			this.availableStatus = ResaleAvailableStatus.DISABLED;
			this.canceledAt = LocalDateTime.now();
		}
	}

	public void updateDailyBasePrice(Integer newBasePrice) {
		Preconditions.domainValidate(newBasePrice != null && newBasePrice > 0, "새로운 기준가는 0보다 커야 합니다.");
		this.dailyBasePrice = newBasePrice;
	}

	public boolean isCancelable() {
		return this.listingStatus == ResaleListingStatus.LISTING
			&& this.availableStatus == ResaleAvailableStatus.ENABLED;
	}

	public boolean isPurchasable() {
		return this.listingStatus == ResaleListingStatus.LISTING
			&& this.availableStatus == ResaleAvailableStatus.ENABLED;
	}
}
