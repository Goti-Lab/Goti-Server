package com.goti.user.dto.request;

import java.time.LocalDate;

import com.goti.constants.Gender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

public record CreateTestUserRequest(
	@NotBlank String name,
	@NotBlank String mobile,
	@NotNull Gender gender,
	@NotNull @Past LocalDate birthDate
) {
}
