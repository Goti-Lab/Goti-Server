package com.goti.payment.service.domain;

import java.util.UUID;

import com.goti.exception.CustomException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.payment.constants.PaymentMethod;
import com.goti.payment.constants.PaymentType;
import com.goti.constants.messages.ErrorCode;
import com.goti.global.validation.Preconditions;
import com.goti.payment.domain.entity.payment.PaymentEntity;
import com.goti.payment.dto.response.PaymentResponse;
import com.goti.payment.repository.PaymentRepository;

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

		return PaymentResponse.from(payment);
	}

	@Override
	@Transactional(readOnly = true)
	public PaymentResponse getByOrderId(UUID orderId) {
		PaymentEntity payment = paymentRepository.findLatestByOrderId(orderId)
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

		return PaymentResponse.from(payment);
	}

	@Override
	@Transactional
	public PaymentResponse cancel(
		UUID orderId,
		UUID cancellationId
	) {
		PaymentEntity payment = paymentRepository.findLatestByOrderId(orderId)
			.orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

		payment.cancel(cancellationId);

		return PaymentResponse.from(payment);
	}

	private boolean shouldFail(String idempotencyKey) {
		return idempotencyKey.toLowerCase().contains("fail");
	}

	private String generateMockPgTid() {
		return "mock-" + UUID.randomUUID();
	}
}
