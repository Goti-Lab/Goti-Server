package com.goti.resale.service.application;

public record ResaleHoldExpiryBatchResult(
	int attempted,
	int succeeded,
	int failed
) {
}
