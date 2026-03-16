package com.goti.resale.domain.entity.resale;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.resale.constants.EscrowStatus;
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
@Table(name = "escrow_accounts")
@NoArgsConstructor(access = PROTECTED)
public class EscrowAccountEntity extends ModificationTimestampEntity {
	@Column(nullable = false)
	private UUID transactionId;

	@Column(nullable = false)
	private UUID buyerId;

	@Column(nullable = false)
	private UUID sellerId;

	@Column(nullable = false)
	private Integer escrowAmount;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private EscrowStatus escrowStatus;

	private LocalDateTime releasedAt;

	private EscrowAccountEntity(
		UUID transactionId,
		UUID buyerId,
		UUID sellerId,
		Integer escrowAmount
	) {
		this.transactionId = transactionId;
		this.buyerId = buyerId;
		this.sellerId = sellerId;
		this.escrowAmount = escrowAmount;
		this.escrowStatus = EscrowStatus.HOLDING;
		this.releasedAt = null;
	}

	public static EscrowAccountEntity create(
		UUID transactionId,
		UUID buyerId,
		UUID sellerId,
		Integer escrowAmount
	) {
		validate(transactionId, buyerId, sellerId, escrowAmount);

		return new EscrowAccountEntity(
			transactionId,
			buyerId,
			sellerId,
			escrowAmount
		);
	}

	private static void validate(
		UUID transactionId,
		UUID buyerId,
		UUID sellerId,
		Integer escrowAmount
	) {
		Preconditions.domainValidate(transactionId != null, "거래 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(buyerId != null, "구매자 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(sellerId != null, "판매자 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(escrowAmount != null && escrowAmount >= 0, "에스크로 금액은 0 이상이어야 합니다.");
	}

}
