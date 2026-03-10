package com.goti.service.application;

public record ResaleHoldExpiryBatchResult(
	int attempted,
	int succeeded,
	int failed
) {
}
