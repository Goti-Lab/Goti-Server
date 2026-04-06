package com.goti.payment.domain.entity.payment;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.global.validation.Preconditions;
import com.goti.payment.constants.EscrowStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	private Long escrowAmount;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private EscrowStatus escrowStatus;

	@Column
	private String externalEscrowId;

	private LocalDateTime releasedAt;

	private EscrowAccountEntity(
		UUID transactionId,
		UUID buyerId,
		UUID sellerId,
		Long escrowAmount
	) {
		this.transactionId = transactionId;
		this.buyerId = buyerId;
		this.sellerId = sellerId;
		this.escrowAmount = escrowAmount;
		this.escrowStatus = EscrowStatus.HOLDING;
		this.externalEscrowId = null;
		this.releasedAt = null;
	}

	public static EscrowAccountEntity create(
		UUID transactionId,
		UUID buyerId,
		UUID sellerId,
		Long escrowAmount
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
		Long escrowAmount
	) {
		Preconditions.domainValidate(transactionId != null, "거래 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(buyerId != null, "구매자 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(sellerId != null, "판매자 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(escrowAmount != null && escrowAmount >= 0, "에스크로 금액은 0 이상이어야 합니다.");
	}

	public void updateExternalId(String externalEscrowId) {
		this.externalEscrowId = externalEscrowId;
	}

	public void settle(LocalDateTime releaseTime) {
		log.info("정산 시도 - 에스크로 ID: {}, 현재 상태: {}", this.getId(), this.escrowStatus);

		if (this.escrowStatus == EscrowStatus.SETTLED) {
			log.info("이미 정산된 에스크로 - ID: {}, 정산 완료 일시: {}", this.getId(), this.releasedAt);
			return;
		}

		Preconditions.domainValidate(this.escrowStatus == EscrowStatus.HOLDING,
			"대기 상태의 에스크로만 정산할 수 있습니다.");

		this.escrowStatus = EscrowStatus.SETTLED;
		this.releasedAt = releaseTime;
	}
}