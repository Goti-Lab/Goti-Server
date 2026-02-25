package com.goti.constants.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;

import java.text.MessageFormat;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
	INVALID_DOMAIN_FIELD(HttpStatus.BAD_REQUEST, "도메인 필드 오류 : {0}"),
	BODY_FIELD_ERROR(HttpStatus.BAD_REQUEST, "바디 필드 오류 : {0}"),

	TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "{0} 타입 오류"),
	MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "{0} 파라미터 필요"),
	INVALID_FORMAT(HttpStatus.BAD_REQUEST, "{0} 형식 오류"),
	INVALID_PROVIDER_TYPE(HttpStatus.BAD_REQUEST, "{0} 은(는) 지원하지 않는 소셜 서비스입니다."),
	INVALID_STATE(HttpStatus.BAD_REQUEST, "유효하지 않은 state입니다."),

	AUTH_INVALID_ACCESS_PATH(HttpStatus.UNAUTHORIZED, "올바르지 않은 접근 경로입니다."),
	AUTH_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	AUTH_INVALID(HttpStatus.UNAUTHORIZED, "올바르지 않은 인증 정보입니다."),
	AUTH_ACCESS_EXPIRED(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 만료되었습니다."),

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생하였습니다. 잠시 후 다시 시도해주세요.")
	;


	private final HttpStatus status;
	private final String message;

	public String format(Object... args) {
		return MessageFormat.format(this.message, args);
	}

	public boolean isSystemError() {
		return status.is5xxServerError();
	}
}
