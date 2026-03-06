package com.goti.dto.response;

public record ResalePaymentResponse(
	String paymentUrl,
	String message
) {
	public static ResalePaymentResponse page() {
		return new ResalePaymentResponse(
			"https://payment_url",
			"결제 페이지로 이동합니다."
		);
	}
}
