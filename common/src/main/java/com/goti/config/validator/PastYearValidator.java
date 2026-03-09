package com.goti.config.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class PastYearValidator implements ConstraintValidator<PastYear, Integer> {
	@Override
	public boolean isValid(Integer value, ConstraintValidatorContext context) {
		if (value == null) return true;

		int currentYear = LocalDate.now().getYear();
		return value <= currentYear;
	}
}
