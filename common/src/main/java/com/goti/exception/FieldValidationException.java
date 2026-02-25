package com.goti.exception;

import com.goti.constants.messages.ErrorCode;

import lombok.Getter;

public class FieldValidationException extends BaseException {

	@Getter
	private final String message;

	public FieldValidationException(ErrorCode error, String message) {
		super(error);
		this.message = error.format(message);
	}
}
