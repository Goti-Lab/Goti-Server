package com.goti.ticketing.order.service.domain;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.goti.global.validation.Preconditions;
import com.goti.ticketing.constants.OrderCancellationRequestType;

@Component
public class OrderCancellationRefundPolicy {

	private static final int CANCELLATION_DEADLINE_HOURS = 4;
	private static final double NEXT_DAY_CANCELLATION_FEE_RATE = 0.1;

	public RefundAmount calculate(
		LocalDateTime paidAt,
		LocalDateTime gameStartAt,
		LocalDateTime canceledAt,
		Integer ticketAmount,
		Integer bookingFeeAmount,
		OrderCancellationRequestType requestType,
		boolean refundableByGameCanceled
	) {
		validate(
			paidAt,
			gameStartAt,
			canceledAt,
			ticketAmount,
			bookingFeeAmount,
			requestType
		);

		if (refundableByGameCanceled) {
			return new RefundAmount(
				ticketAmount + bookingFeeAmount,
				0,
				bookingFeeAmount
			);
		}

		Preconditions.domainValidate(
			canceledAt.isBefore(gameStartAt.minusHours(CANCELLATION_DEADLINE_HOURS)),
			"경기 시작 4시간 전까지만 취소할 수 있습니다."
		);

		if (paidAt.toLocalDate().isEqual(canceledAt.toLocalDate())) {
			int refundedBookingFeeAmount = requestType == OrderCancellationRequestType.ORDER_FULL
				? bookingFeeAmount
				: 0;
			return new RefundAmount(
				ticketAmount + refundedBookingFeeAmount,
				0,
				refundedBookingFeeAmount
			);
		}

		int cancellationFeeAmount = (int) Math.floor(ticketAmount * NEXT_DAY_CANCELLATION_FEE_RATE);
		return new RefundAmount(
			ticketAmount - cancellationFeeAmount,
			cancellationFeeAmount,
			0
		);
	}

	private void validate(
		LocalDateTime paidAt,
		LocalDateTime gameStartAt,
		LocalDateTime canceledAt,
		Integer ticketAmount,
		Integer bookingFeeAmount,
		OrderCancellationRequestType requestType
	) {
		Preconditions.domainValidate(
			paidAt != null,
			"결제 완료 일시는 필수입니다."
		);
		Preconditions.domainValidate(
			gameStartAt != null,
			"경기 시작 일시는 필수입니다."
		);
		Preconditions.domainValidate(
			canceledAt != null,
			"취소 요청 일시는 필수입니다."
		);
		Preconditions.domainValidate(
			ticketAmount != null && ticketAmount >= 0,
			"티켓 금액은 0원 이상이어야 합니다."
		);
		Preconditions.domainValidate(
			bookingFeeAmount != null && bookingFeeAmount >= 0,
			"예매 수수료는 0원 이상이어야 합니다."
		);
		Preconditions.domainValidate(
			requestType != null,
			"취소 요청 타입은 필수입니다."
		);
	}

	public record RefundAmount(
		Integer refundAmount,
		Integer cancellationFeeAmount,
		Integer refundedBookingFeeAmount
	) {
	}
}
