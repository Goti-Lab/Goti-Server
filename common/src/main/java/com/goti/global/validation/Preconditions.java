package com.goti.global.validation;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.exception.FieldValidationException;

public class Preconditions {
	public static void domainValidate(boolean expression, String message) {
		if (!expression) {
			throw new FieldValidationException(ErrorCode.INVALID_DOMAIN_FIELD, message);
		}
	}

	public static void validate(boolean expression, ErrorCode errorCode) {
		if (!expression) {
			throw new CustomException(errorCode);
		}
	}

	public static void validate(boolean expression, ErrorCode errorCode, String message) {
		if (!expression) {
			throw new CustomException(errorCode, message);
		}
	}
}
