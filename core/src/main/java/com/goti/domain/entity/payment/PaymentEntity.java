package com.goti.domain.entity.payment;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.util.StringUtils;

import com.goti.constants.PaymentMethod;
import com.goti.constants.PaymentStatus;
import com.goti.constants.PaymentType;
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
@Table(
	name = "payments",
	indexes = {
		@Index(name = "idx_payments_pg_tid", columnList = "pg_tid"),
		@Index(name = "idx_payments_payment_status", columnList = "payment_status")
	}
)
@NoArgsConstructor(access = PROTECTED)
public class PaymentEntity extends ModificationTimestampEntity {

	@Column(name = "order_id", nullable = false)
	private UUID orderId;

	@Column(name = "cancellation_id")
	private UUID cancellationId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentType paymentType;

	@Enumerated(EnumType.STRING)
	private PaymentMethod paymentMethod;

	@Column(nullable = false)
	private Integer paymentAmount;

	@Column
	private String pgProvider;

	@Column
	private String pgTid;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PaymentStatus paymentStatus;

	private LocalDateTime paidAt;

	@Column(columnDefinition = "TEXT")
	private String failedReason;

	@Column(nullable = false, unique = true)
	private String idempotencyKey;

	private PaymentEntity(
		UUID orderId,
		UUID cancellationId,
		PaymentType paymentType,
		PaymentMethod paymentMethod,
		Integer paymentAmount,
		String pgProvider,
		String pgTid,
		String idempotencyKey
	) {
		this.orderId = orderId;
		this.cancellationId = cancellationId;
		this.paymentType = paymentType;
		this.paymentMethod = paymentMethod;
		this.paymentAmount = paymentAmount;
		this.pgProvider = pgProvider;
		this.pgTid = pgTid;
		this.paymentStatus = PaymentStatus.PENDING;
		this.paidAt = null;
		this.idempotencyKey = idempotencyKey;
	}

	public static PaymentEntity create(
		UUID orderId,
		UUID cancellationId,
		PaymentType paymentType,
		PaymentMethod paymentMethod,
		Integer paymentAmount,
		String pgProvider,
		String pgTid,
		String idempotencyKey
	) {
		validate(
			orderId,
			cancellationId,
			paymentType,
			paymentMethod,
			paymentAmount,
			idempotencyKey
		);
		return new PaymentEntity(
			orderId,
			cancellationId,
			paymentType,
			paymentMethod,
			paymentAmount,
			pgProvider,
			pgTid,
			idempotencyKey
		);
	}

	public void succeed(String pgTid) {
		Preconditions.domainValidate(
			this.paymentStatus == PaymentStatus.PENDING,
			"PENDING 상태에서만 결제 성공 처리할 수 있습니다."
		);
		this.paymentStatus = PaymentStatus.SUCCESS;
		this.pgTid = pgTid;
		this.paidAt = LocalDateTime.now();
		this.failedReason = null;
	}

	public void fail(String failedReason) {
		Preconditions.domainValidate(
			this.paymentStatus == PaymentStatus.PENDING,
			"PENDING 상태에서만 결제 실패 처리할 수 있습니다."
		);
		Preconditions.domainValidate(
			StringUtils.hasText(failedReason),
			"결제 실패 사유는 비어 있을 수 없습니다."
		);
		this.paymentStatus = PaymentStatus.FAILED;
		this.paidAt = null;
		this.failedReason = failedReason;
	}

	private static void validate(
		UUID orderId,
		UUID cancellationId,
		PaymentType paymentType,
		PaymentMethod paymentMethod,
		Integer paymentAmount,
		String idempotencyKey
	) {
		Preconditions.domainValidate(
			orderId != null,
			"주문 ID는 필수입니다."
		);
		Preconditions.domainValidate(
			paymentType != null,
			"결제 처리 유형은 필수입니다."
		);
		Preconditions.domainValidate(
			paymentAmount != null && paymentAmount > 0,
			"결제 금액은 0원보다 커야 합니다."
		);
		Preconditions.domainValidate(
			StringUtils.hasText(idempotencyKey),
			"멱등 키는 비어 있을 수 없습니다."
		);
		if (paymentType == PaymentType.PAYMENT) {
			Preconditions.domainValidate(
				paymentMethod != null,
				"결제 유형이 '결제'인 경우 결제 수단은 필수입니다."
			);
		}

		if (paymentType == PaymentType.REFUND) {
			Preconditions.domainValidate(
				cancellationId != null,
				"결제 유형이 '환불'인 경우 취소 정보는 필수입니다."
			);
		}
	}
}
