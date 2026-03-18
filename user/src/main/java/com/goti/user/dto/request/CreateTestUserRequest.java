package com.goti.user.dto.request;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.goti.constants.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;

public record CreateTestUserRequest(

	@NotBlank(message = "이름은 필수 항목입니다.")
	String name,

	@NotBlank(message = "휴대전화번호는 필수 항목입니다.")
	@Pattern(regexp = "^000\\d{8}$", message = "테스트 유저 mobile은 000XXXXXXXX 형식이어야 합니다")
	String mobile,

	@NotNull(message = "성별은 필수 항목입니다.")
	Gender gender,

	@NotNull(message = "생년월일은 필수 항목입니다.")
	@Past(message = "유효하지 않은 생년월일입니다.")
	@JsonFormat(pattern = "yyyy-MM-dd")
	LocalDate birthDate
) {
}
