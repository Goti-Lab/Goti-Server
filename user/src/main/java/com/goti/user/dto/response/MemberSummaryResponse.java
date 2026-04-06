package com.goti.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 정보(본인) 조회 요약 응답")
public record MemberSummaryResponse(
	@Schema(description = "이름", example = "홍길동")
	String name,

	@Schema(description = "이메일", example = "hong@example.com")
	String email,

	@Schema(description = "휴대폰 번호", example = "010-1234-5678")
	String mobile
) {
	public static MemberSummaryResponse of(String name, String email, String mobile) {
		return new MemberSummaryResponse(name, email, mobile);
	}
}
