package com.goti.domain.entity.payment;

import static lombok.AccessLevel.*;

import java.time.LocalDateTime;

import org.springframework.util.StringUtils;

import com.goti.constants.PaymentMethod;
import com.goti.constants.PaymentStatus;
import com.goti.constants.PaymentType;
import com.goti.domain.base.ModificationTimestampEntity;
import com.goti.domain.entity.order.OrderCancellationEntity;
import com.goti.domain.entity.order.OrderEntity;
import com.goti.global.validation.Preconditions;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private OrderEntity order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "cancellation_id")
	private OrderCancellationEntity cancellation;

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

	@Column(nullable = false, unique = true)
	private String idempotencyKey;

	private PaymentEntity(
		OrderEntity order,
		OrderCancellationEntity cancellation,
		PaymentType paymentType,
		PaymentMethod paymentMethod,
		Integer paymentAmount,
		String pgProvider,
		String pgTid,
		String idempotencyKey
	) {
		this.order = order;
		this.cancellation = cancellation;
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
		OrderEntity order,
		OrderCancellationEntity cancellation,
		PaymentType paymentType,
		PaymentMethod paymentMethod,
		Integer paymentAmount,
		String pgProvider,
		String pgTid,
		String idempotencyKey
	) {
		validate(
			cancellation,
			paymentType,
			paymentMethod,
			paymentAmount,
			idempotencyKey
		);
		return new PaymentEntity(
			order,
			cancellation,
			paymentType,
			paymentMethod,
			paymentAmount,
			pgProvider,
			pgTid,
			idempotencyKey
		);
	}

	private static void validate(
		OrderCancellationEntity cancellation,
		PaymentType paymentType,
		PaymentMethod paymentMethod,
		Integer paymentAmount,
		String idempotencyKey
	) {
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
				cancellation != null,
				"결제 유형이 '환불'인 경우 취소 정보는 필수입니다."
			);
		}
	}
}
