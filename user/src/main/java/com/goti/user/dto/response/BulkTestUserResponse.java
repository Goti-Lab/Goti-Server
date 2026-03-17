package com.goti.user.dto.response;

public record BulkTestUserResponse(
	int createdCount,
	int skippedCount
) {
}
