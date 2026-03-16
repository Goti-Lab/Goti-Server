package com.goti.service.domain;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.PaymentMethod;
import com.goti.constants.PaymentType;
import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;
import com.goti.domain.entity.payment.PaymentEntity;
import com.goti.dto.response.PaymentResponse;
import com.goti.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
	private static final String MOCK_PG_PROVIDER = "MOCK";
	private static final String MOCK_PAYMENT_FAILED_REASON = "mock 결제 실패";

	private final PaymentRepository paymentRepository;

	@Override
	@Transactional
	public PaymentResponse create(
		UUID orderId,
		UUID userId,
		PaymentMethod paymentMethod,
		String idempotencyKey,
		Integer paymentAmount
	) {
		Preconditions.validate(
			userId != null,
			ErrorCode.AUTH_INVALID
		);

		Preconditions.validate(
			!paymentRepository.existsByIdempotencyKey(idempotencyKey),
			ErrorCode.PAYMENT_IDEMPOTENCY_KEY_ALREADY_EXISTS
		);

		PaymentEntity payment = PaymentEntity.create(
			orderId,
			null,
			PaymentType.PAYMENT,
			paymentMethod,
			paymentAmount,
			MOCK_PG_PROVIDER,
			null,
			idempotencyKey
		);

		if (shouldFail(idempotencyKey)) {
			payment.fail(MOCK_PAYMENT_FAILED_REASON);
			paymentRepository.save(payment);
		} else {
			payment.succeed(generateMockPgTid());
			paymentRepository.save(payment);
		}

		return PaymentResponse.from(
			payment.getId(),
			payment.getOrderId(),
			payment.getPaymentType(),
			payment.getPaymentMethod(),
			payment.getPaymentAmount(),
			payment.getPgProvider(),
			payment.getPgTid(),
			payment.getPaymentStatus(),
			payment.getPaidAt(),
			payment.getFailedReason()
		);
	}

	private boolean shouldFail(String idempotencyKey) {
		return idempotencyKey.toLowerCase().contains("fail");
	}

	private String generateMockPgTid() {
		return "mock-" + UUID.randomUUID();
	}
}
