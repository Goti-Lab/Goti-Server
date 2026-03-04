package com.goti.config.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MobileValidator implements ConstraintValidator<ValidMobile, String> {

	final static int MOBILE_LENGTH = 11;
	final static String MOBILE_PREFIX = "010";

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value.length() != MOBILE_LENGTH || !value.startsWith(MOBILE_PREFIX)) {
			return false;
		}

		String body = value.substring(3);

		for (char c : body.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}

		return true;
	}
}