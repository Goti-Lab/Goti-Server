package com.goti.user.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record BulkCreateTestUserRequest(
	@Min(1) @Max(100_000) int count,
	@Min(1) int startIndex
) {
}
