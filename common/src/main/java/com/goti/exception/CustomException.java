package com.goti.exception;


import com.goti.constants.messages.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomException extends BaseException {

	public CustomException(ErrorCode error) {
		super(error);
	}

	public CustomException(ErrorCode error, String message) {
		super(error, message);
	}

	public CustomException(ErrorCode error, Throwable cause) {
		super(error, cause);
	}

}

