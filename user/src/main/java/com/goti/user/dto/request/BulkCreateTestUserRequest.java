package com.goti.user.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BulkCreateTestUserRequest(

	@NotNull(message = "생성 건수는 필수 항목입니다.")
	@Min(value = 1, message = "최소 1건 이상이어야 합니다.")
	@Max(value = 10_000, message = "최대 10,000건까지 가능합니다. 더 필요하면 startIndex를 변경하여 반복 호출하세요.")
	Integer count,

	@NotNull(message = "시작 인덱스는 필수 항목입니다.")
	@Min(value = 1, message = "시작 인덱스는 1 이상이어야 합니다.")
	Integer startIndex
) {

	private static final int MAX_MOBILE_INDEX = 100_000_000;

	@AssertTrue(message = "startIndex + count가 100,000,000을 초과할 수 없습니다.")
	boolean isIndexRangeValid() {
		if (startIndex == null || count == null) {
			return true;
		}
		return (long) startIndex + count <= MAX_MOBILE_INDEX;
	}
}
