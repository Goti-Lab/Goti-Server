package com.goti.exception;

import com.goti.constants.messages.ErrorCode;

import org.springframework.core.NestedRuntimeException;

import lombok.Getter;
import lombok.experimental.Accessors;

public abstract class BaseException extends NestedRuntimeException {

	@Getter
	@Accessors(fluent = true)
	private ErrorCode error;

	protected BaseException(ErrorCode error) {
		super(error.getMessage());
		this.error = error;
	}

	protected BaseException(ErrorCode error, String message) {
		super(error.format(message));
		this.error = error;
	}

	protected BaseException(ErrorCode error, Throwable cause) {
		super(error.getMessage(), cause);
		this.error = error;
	}
}
