package com.goti.exception.handler.base;

import com.goti.global.api.ApiErrorResponse;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.BaseException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class BaseExceptionHandler {
	public ResponseEntity<ApiErrorResponse> toResponse(BaseException ex) {
		HttpStatus status = ex.error().getStatus();
		String message = ex.getMessage();
		return toResponse(status, message);
	}

	public ResponseEntity<ApiErrorResponse> toResponse(ErrorCode errorCode) {
		HttpStatus status = errorCode.getStatus();
		String message = errorCode.getMessage();
		return toResponse(status, message);
	}

	public ResponseEntity<ApiErrorResponse> toResponse(ErrorCode errorCode, Object... args) {
		HttpStatus status = errorCode.getStatus();
		String message = errorCode.format(args);
		return toResponse(status, message);
	}

	public ResponseEntity<ApiErrorResponse> toResponse(HttpStatus status, String message) {
		return ApiErrorResponse.error(status, message);
	}

}
