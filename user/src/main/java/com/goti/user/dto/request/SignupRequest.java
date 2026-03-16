package com.goti.user.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goti.config.validator.ValidAuthCode;
import com.goti.config.validator.ValidMobile;
import com.goti.constants.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record SignupRequest(

	@NotBlank(message = "소셜 검증 토큰은 필수 항목입니다.")
	String socialVerifyToken,

	@NotBlank(message = "이름은 필수 항목입니다.")
	String name,

	@ValidMobile
	@NotBlank(message = "휴대전화번호는 필수 항목입니다.")
	String mobile,

	@NotNull(message = "성별은 필수 항목입니다.")
	Gender gender,

	@NotNull(message = "생년월일은 필수 항목입니다.")
	@Past(message = "유효하지 않은 생년월일입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate birthDate,

	@ValidAuthCode
	@NotBlank(message = "본인확인 인증코드는 필수 항목입니다.")
	String authCode
) {
}
