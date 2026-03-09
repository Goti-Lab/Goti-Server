package com.goti.domain.entity.pricing;

import static lombok.AccessLevel.*;

import java.time.LocalDate;
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
@Table(name = "ticket_pricing_policies")
@NoArgsConstructor(access = PROTECTED)
public class TicketPricingPolicyEntity extends ModificationTimestampEntity {

	@Column(nullable = false)
	private UUID teamId;

	@Column(nullable = false)
	private LocalDate policyStartAt;

	@Column(nullable = false)
	private LocalDate policyEndAt;

	@Column(nullable = false)
	private boolean isActive;

	@Column(nullable = false)
	private UUID createdBy;

	private TicketPricingPolicyEntity(
		UUID teamId,
		LocalDate policyStartAt,
		LocalDate policyEndAt,
		UUID createdBy
	) {
		this.teamId = teamId;
		this.policyStartAt = policyStartAt;
		this.policyEndAt = policyEndAt;
		this.isActive = true;
		this.createdBy = createdBy;
	}

	public static TicketPricingPolicyEntity create(
		UUID teamId,
		LocalDate policyStartAt,
		LocalDate policyEndAt,
		UUID createdBy
	) {
		validate(teamId, policyStartAt, policyEndAt, createdBy);
		return new TicketPricingPolicyEntity(
			teamId,
			policyStartAt,
			policyEndAt,
			createdBy
		);
	}

	private static void validate(
		UUID teamId,
		LocalDate policyStartAt,
		LocalDate policyEndAt,
		UUID createdBy
	) {
		Preconditions.domainValidate(
			teamId != null,
			"팀 ID는 필수입니다."
		);
		Preconditions.domainValidate(
			policyStartAt != null,
			"정책 시작일은 필수입니다."
		);
		Preconditions.domainValidate(
			policyEndAt != null,
			"정책 종료일은 필수입니다."
		);
		Preconditions.domainValidate(
			!policyEndAt.isBefore(policyStartAt),
			"정책 종료일은 시작일보다 빠를 수 없습니다."
		);
		Preconditions.domainValidate(
			createdBy != null,
			"생성자 ID는 필수입니다."
		);
	}
}
