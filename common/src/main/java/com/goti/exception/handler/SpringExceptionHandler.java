package com.goti.exception.handler;

import java.util.Objects;

import com.goti.global.api.ApiErrorResponse;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.handler.base.BaseExceptionHandler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@RestControllerAdvice
public class SpringExceptionHandler extends BaseExceptionHandler {

	/**
	 * - @Valid 검증 실패 (@RequestBody)
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
		FieldError fieldError = ex.getBindingResult().getFieldError();
		String field = Objects.requireNonNull(fieldError).getField();
		String message = fieldError.getDefaultMessage();

		log.warn("[Validation Failed] field={} message={}", field, message, ex);
		return toResponse(ErrorCode.BODY_FIELD_ERROR, message);
	}

	/**
	 * - @RequestParam, @PathVariable 바인딩 실패
	 */
	@ExceptionHandler(BindException.class)
	public ResponseEntity<ApiErrorResponse> handleBind(BindException ex) {
		FieldError fieldError = ex.getBindingResult().getFieldError();
		String field = Objects.requireNonNull(fieldError).getField();
		String message = fieldError.getDefaultMessage();

		log.warn("[Binding Failed] field={} message={}", field, message, ex);
		return toResponse(ErrorCode.INVALID_FORMAT, field);
	}

	/**
	 * - @RequestParam 타입 불일치
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(
		MethodArgumentTypeMismatchException ex
	) {
		String paramName = ex.getName();
		String requiredType =
			ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "Unknown";
		String value = ex.getValue() != null ? ex.getValue().toString() : "null";

		log.warn("[Type Mismatch] param={} value={} requiredType={}", paramName, value, requiredType, ex);
		return toResponse(ErrorCode.TYPE_MISMATCH, paramName);
	}

	/**
	 * - 필수 파라미터 누락
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public ResponseEntity<ApiErrorResponse> handleMissingParameter(
		MissingServletRequestParameterException ex
	) {
		String paramName = ex.getParameterName();

		log.warn("[Missing Parameter] param={}", paramName, ex);
		return toResponse(ErrorCode.MISSING_PARAMETER, paramName);
	}

	/**
	 * - JSON 파싱 실패
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadable(
		HttpMessageNotReadableException ex
	) {
		log.warn("[JSON Parse Failed] message={}", ex.getMessage(), ex);
		return toResponse(ErrorCode.BAD_REQUEST);
	}

	/**
	 * - 지원하지 않는 HTTP Method
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupported(
		HttpRequestMethodNotSupportedException ex
	) {
		String method = ex.getMethod();

		log.warn("[Method Not Supported] method={}", method, ex);
		return toResponse(ErrorCode.BAD_REQUEST);
	}

	/**
	 * - 존재하지 않는 엔드포인트
	 */
	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex) {
		String url = ex.getRequestURL();

		log.warn("[Not Found] url={}", url, ex);
		return toResponse(ErrorCode.BAD_REQUEST);
	}

	/**
	 * - 데이터베이스 예외 포함 기타 모든 서버 에러 처리
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleServerError(Exception ex) {
		log.error("[Server Error] type={} message={}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
		return toResponse(ErrorCode.INTERNAL_SERVER_ERROR);
	}

}
