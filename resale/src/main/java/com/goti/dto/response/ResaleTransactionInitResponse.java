package com.goti.dto.response;

public record ResaleTransactionInitResponse(
	String paymentUrl,
	String message
) {
	public static ResaleTransactionInitResponse page() {
		return new ResaleTransactionInitResponse(
			"https://payment_url",
			"결제 페이지로 이동합니다."
		);
	}
}
