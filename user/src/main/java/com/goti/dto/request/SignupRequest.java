package com.goti.dto.request;

import com.goti.config.validator.ValidMobile;
import com.goti.constants.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record SignupRequest(
	@NotBlank(message = "이름은 필수 항목입니다.")
	String name,

	@ValidMobile
	@NotBlank(message = "휴대전화번호는 필수 항목입니다.")
	String mobile,

	@NotNull(message = "성별은 필수 항목입니다.")
	Gender gender,

	@NotNull(message = "생년월일은 필수 항목입니다.")
	@Pattern(
		regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$",
		message = "생년월일 형식이 올바르지 않습니다. (yyyy-MM-dd)"
	)
	LocalDate birthDate
) {
}
