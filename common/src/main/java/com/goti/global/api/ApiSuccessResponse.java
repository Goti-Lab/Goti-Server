package com.goti.global.api;

import com.fasterxml.jackson.annotation.JsonInclude;

import com.goti.constants.messages.SuccessCode;

import lombok.Getter;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class ApiSuccessResponse<T> extends ApiResponse {

	final static HttpStatus SUCCESS = HttpStatus.OK;

	@JsonInclude(JsonInclude.Include.NON_NULL)
	private final T data;

	public ApiSuccessResponse(String code, String message, T data) {
		super(code, message);
		this.data = data;
	}

	public static ResponseEntity<ApiSuccessResponse<Void>> empty() {
		return wrap(null);
	}

	public static <T> ResponseEntity<ApiSuccessResponse<T>> wrap(T data) {
		return ResponseEntity.status(SUCCESS)
			.body(
				new ApiSuccessResponse<>(
					SuccessCode.RESULT.getCode(),
					SuccessCode.RESULT.getMessage(),
					data
				)
			);
	}

	public static <T> ResponseEntity<ApiSuccessResponse<PageResponse<T>>> page(Page<T> data) {
		return wrap(new PageResponse<>(data));
	}
}
