package com.goti.user.dto.response;

import com.goti.constants.Gender;

import com.goti.user.domain.entity.user.MemberEntity;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "회원 정보 수정 응답")
public record MemberUpdateResponse(

	@Schema(description = "휴대폰 번호", example = "01099998888")
	String mobile,

	@Schema(description = "이름", example = "홍길동")
	String name,

	@Schema(description = "성별 (MALE, FEMALE)", example = "MALE")
	Gender gender,

	@Schema(description = "생년월일", example = "2000-01-01")
	LocalDate birthDate
) {
	public static MemberUpdateResponse from(MemberEntity member) {
		return new MemberUpdateResponse(
			member.getMobile(),
			member.getName(),
			member.getGender(),
			member.getBirthDate()
		);
	}
}
