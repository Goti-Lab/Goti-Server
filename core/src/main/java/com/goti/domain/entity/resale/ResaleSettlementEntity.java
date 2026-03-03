package com.goti.domain.entity.resale;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;
import java.util.UUID;

import com.goti.constants.ResaleSettlementStatus;
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
@Table(name = "resale_settlements")
@NoArgsConstructor(access = PROTECTED)
public class ResaleSettlementEntity extends ModificationTimestampEntity {
	@Column(nullable = false)
	private UUID escrowId;

	@Column(nullable = false)
	private UUID sellerId;

	@Column(nullable = false)
	private Integer settlementAmount;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ResaleSettlementStatus SettlementStatus;

	private String bankName;

	private String accountNumber;

	private String accountHolder;

	private LocalDateTime settledAt;

	private String failedReason;

	private ResaleSettlementEntity(
		UUID escrowId,
		UUID sellerId,
		Integer settlementAmount
	) {
		this.escrowId = escrowId;
		this.sellerId = sellerId;
		this.settlementAmount = settlementAmount;
		this.SettlementStatus = ResaleSettlementStatus.PENDING;
		this.bankName = null;
		this.accountNumber = null;
		this.accountHolder = null;
		this.settledAt = null;
		this.failedReason = null;
	}

	public static ResaleSettlementEntity create(
		UUID escrowId,
		UUID sellerId,
		Integer settlementAmount
	) {
		validate(escrowId, sellerId, settlementAmount);

		return new ResaleSettlementEntity(
			escrowId,
			sellerId,
			settlementAmount);
	}

	private static void validate(
		UUID escrowId,
		UUID sellerId,
		Integer settlementAmount
	) {
		Preconditions.domainValidate(escrowId != null, "에스크로 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(sellerId != null, "판매자 ID는 비어 있을 수 없습니다.");
		Preconditions.domainValidate(settlementAmount != null && settlementAmount >= 0, "정산 금액은 0 이상이어야 합니다");
	}
}
