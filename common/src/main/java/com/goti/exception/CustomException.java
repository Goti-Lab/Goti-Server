package com.goti.exception;


import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.goti.constants.messages.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomException extends BaseException {

	private final Map<String, Object> context = new LinkedHashMap<>();

	public CustomException(ErrorCode error) {
		super(error);
	}

	public CustomException(ErrorCode error, String message) {
		super(error, message);
	}

	public CustomException(ErrorCode error, Throwable cause) {
		super(error, cause);
	}

	public CustomException withContext(String key, Object value) {
		context.put(key, value);
		return this;
	}

	public Map<String, Object> context() {
		return Collections.unmodifiableMap(context);
	}

}
