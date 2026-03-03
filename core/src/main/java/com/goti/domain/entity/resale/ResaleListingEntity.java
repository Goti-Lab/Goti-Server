package com.goti.domain.entity.resale;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;
import java.util.UUID;

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
	private String seatInfo;

	@Column(nullable = false)
	private Integer dailyBasePrice;

	@Column(nullable = false)
	private Integer listingPrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ResaleListingStatus listingStatus;

	private Integer lastTransactionPrice;

	@Column(nullable = false)
	private LocalDateTime listedAt;

	private LocalDateTime soldAt;

	private LocalDateTime canceledAt;

	private LocalDateTime defrostAt;

	public ResaleListingEntity(
		UUID ticketId,
		UUID sellerId,
		UUID gameId,
		String seatInfo,
		Integer dailyBasePrice,
		Integer listingPrice
	) {
		this.ticketId = ticketId;
		this.sellerId = sellerId;
		this.gameId = gameId;
		this.seatInfo = seatInfo;
		this.dailyBasePrice = dailyBasePrice;
		this.listingPrice = listingPrice;
		this.listingStatus = ResaleListingStatus.RESELL_AVAILABLE;
		this.lastTransactionPrice = null;
		this.listedAt = LocalDateTime.now();
		this.soldAt = null;
		this.canceledAt = null;
		this.defrostAt = null;
	}

	public static ResaleListingEntity create(
		UUID ticketId,
		UUID sellerId,
		UUID gameId,
		String seatInfo,
		Integer dailyBasePrice,
		Integer listingPrice
	) {
		validate(ticketId, sellerId, gameId, seatInfo, dailyBasePrice, listingPrice);

		return new ResaleListingEntity(
			ticketId,
			sellerId,
			gameId,
			seatInfo,
			dailyBasePrice,
			listingPrice
		);
	}

	private static void validate(
		UUID ticketId,
		UUID sellerId,
		UUID gameId,
		String seatInfo,
		Integer dailyBasePrice,
		Integer listingPrice
	) {
		Preconditions.domainValidate(ticketId != null, "티켓 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(sellerId != null, "판매자 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(gameId != null, "게임 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(seatInfo != null, "좌석 정보는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(dailyBasePrice != null && dailyBasePrice >= 0, "일일 기준가는 0 이상이어야 합니다.");
		Preconditions.domainValidate(listingPrice != null && listingPrice >= 0, "판매가는 0 이상이어야 합니다.");
	}

	public boolean isCancelable() {
		return this.listingStatus == ResaleListingStatus.RESELL_AVAILABLE;
	}

	public boolean canDefrost() {
		if (this.listingStatus != ResaleListingStatus.FROZEN || this.defrostAt == null) {
			return false;
		}
		return LocalDateTime.now().isAfter(this.defrostAt);
	}

	public boolean isWithinOneHour() {
		return LocalDateTime.now().isBefore(this.listedAt.plusHours(1));
	}

	public void cancelImmediately() {
		Preconditions.domainValidate(isCancelable(), "리셀 가능 상태에서만 취소할 수 있습니다.");
		Preconditions.domainValidate(isWithinOneHour(), "등록 후 1시간 이내에만 즉시 취소가 가능합니다.");

		this.listingStatus = ResaleListingStatus.RESELL_AVAILABLE;
		this.canceledAt = LocalDateTime.now();
	}

	public void freeze() {
		Preconditions.domainValidate(isCancelable(), "리셀 가능 상태에서만 취소할 수 있습니다.");
		Preconditions.domainValidate(!isWithinOneHour(), "등록 1시간 이후에 한 취소는 동결됩니다.");

		this.listingStatus = ResaleListingStatus.FROZEN;
		this.canceledAt = LocalDateTime.now();
		this.defrostAt = LocalDateTime.now().plusHours(12);
	}

	public void defrost() {
		Preconditions.domainValidate(canDefrost(), "아직 동결 해제 시간이 아닙니다.");

		this.listingStatus = ResaleListingStatus.RESELL_AVAILABLE;
		this.defrostAt = null;
	}

	public void cancelByGameStart() {
		if (this.listingStatus == ResaleListingStatus.RESELL_AVAILABLE
			|| this.listingStatus == ResaleListingStatus.FROZEN) {
			this.listingStatus = ResaleListingStatus.CANCELED;
			this.canceledAt = LocalDateTime.now();
		}
	}

	public void SoldOut(Integer transactionPrice) {
		Preconditions.domainValidate(
			this.listingStatus == ResaleListingStatus.RESELL_AVAILABLE,
			"판매 가능한 상태에만 판매할 수 있습니다."
		);

		this.listingStatus = ResaleListingStatus.SOLD;
		this.lastTransactionPrice = transactionPrice;
		this.soldAt = LocalDateTime.now();
	}

	public void updateDailyBasePrice(Integer newBasePrice) {
		Preconditions.domainValidate(newBasePrice != null && newBasePrice > 0, "새로운 기준가는 0보다 커야 합니다.");
		this.dailyBasePrice = newBasePrice;
	}
}
