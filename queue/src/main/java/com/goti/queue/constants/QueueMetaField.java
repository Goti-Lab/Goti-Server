package com.goti.queue.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class QueueMetaField {
	public static final String MAX_CAPACITY = "maxCapacity";
	public static final String ACTIVE_COUNT = "activeCount";
	public static final String PUBLISHED_RANK = "publishedRank";
	public static final String CURRENT_ALLOWED_RANK = "currentAllowedRank";
	public static final String LAST_ENTERED_RANK = "lastEnteredRank";
	public static final String UPDATED_AT = "updatedAt";
}
