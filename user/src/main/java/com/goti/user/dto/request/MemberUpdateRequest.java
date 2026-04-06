package com.goti.user.dto.request;

import com.goti.config.validator.ValidMobile;

import com.goti.constants.Gender;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "회원 정보 수정 요청")
public record MemberUpdateRequest(

	@ValidMobile
	@NotBlank(message = "휴대폰 번호는 필수 항목입니다.")
	@Schema(description = "수정할 휴대폰 번호", example = "01099998888")
	String mobile,

	@NotBlank(message = "이름은 필수 입력 항목입니다.")
	@Schema(description = "수정할 이름", example = "홍길동")
	String name,

	@NotNull(message = "성별은 필수 입력 값입니다.")
	@Schema(description = "성별 (MALE, FEMALE)", example = "MALE")
	Gender gender,

	@NotNull(message = "생년월일은 필수 입력 값입니다.")
	@Schema(description = "생년월일", example = "2000-01-01")
	LocalDate birthDate,

	@NotBlank(message = "인증 코드는 필수 입력 값입니다.")
	@Schema(description = "SMS 인증 코드", example = "123456")
	String authCode

) {
}
