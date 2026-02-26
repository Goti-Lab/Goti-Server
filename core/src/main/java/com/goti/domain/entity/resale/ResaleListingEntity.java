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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "resale_listings")
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
	private Integer originalPrice;

	@Column(nullable = false)
	private Integer listingPrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private ResaleListingStatus listingStatus;

	private Integer lastTransactionPrice;

	private LocalDateTime soldAt;

	private LocalDateTime canceledAt;

	private LocalDateTime defrostAt;

	public ResaleListingEntity(
		UUID ticketId,
		UUID sellerId,
		UUID gameId,
		String seatInfo,
		Integer originalPrice,
		Integer listingPrice
	) {
		this.ticketId = ticketId;
		this.sellerId = sellerId;
		this.gameId = gameId;
		this.seatInfo = seatInfo;
		this.originalPrice = originalPrice;
		this.listingPrice = listingPrice;
		this.listingStatus = ResaleListingStatus.RESELL_AVAILABLE;
		this.lastTransactionPrice = null;
		this.soldAt = null;
		this.canceledAt = null;
		this.defrostAt = null;
	}

	public static ResaleListingEntity create(
		UUID ticketId,
		UUID sellerId,
		UUID gameId,
		String seatInfo,
		Integer originalPrice,
		Integer listingPrice
	) {
		validate(ticketId, sellerId, gameId, seatInfo, originalPrice, listingPrice);

		return new ResaleListingEntity(
			ticketId,
			sellerId,
			gameId,
			seatInfo,
			originalPrice,
			listingPrice
		);
	}

	private static void validate(
		UUID ticketId,
		UUID sellerId,
		UUID gameId,
		String seatInfo,
		Integer originalPrice,
		Integer listingPrice
	) {
		Preconditions.domainValidate(ticketId != null, "티켓 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(sellerId != null, "판매자 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(gameId != null, "게임 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(seatInfo != null, "좌석 정보는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(originalPrice != null && originalPrice >= 0, "원가는 0 이상이어야 합니다.");
		Preconditions.domainValidate(listingPrice != null && listingPrice >= 0, "판매가는 0 이상이어야 합니다.");
	}

}
