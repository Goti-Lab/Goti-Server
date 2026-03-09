package com.goti.constants.messages;

import java.text.MessageFormat;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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

	RESALE_BLOCKED(HttpStatus.FORBIDDEN, "리셀이 차단되었습니다. 차단 해제일을 확인해주세요."),
	DAILY_SELL_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "오늘의 판매 가능 횟수({0}회)를 초과했습니다."),
	DAILY_BUY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "오늘의 구매 가능 횟수({0}회)를 초과했습니다."),
	DAILY_CANCEL_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "오늘의 취소 가능 횟수({0}회)를 초과했습니다."),
	GAME_SELL_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "이 경기에 대한 판매 가능 횟수({0}회)를 초과했습니다."),
	GAME_BUY_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "이 경기에 대한 구매 가능 횟수({0}회)를 초과했습니다."),
	GAME_CANCEL_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "이 경기에 대한 취소 가능 횟수({0}회)를 초과했습니다."),
	INVALID_BASE_PRICE(HttpStatus.BAD_REQUEST, "시작가가 올바르지 않습니다."),
	INVALID_LISTING_PRICE(HttpStatus.BAD_REQUEST, "판매가가 올바르지 않습니다."),
	INVALID_PRICE(HttpStatus.BAD_REQUEST, "가격이 올바르지 않습니다."),
	INVALID_PRICE_RANGE(HttpStatus.BAD_REQUEST, "판매가는 {0} 이내여야 합니다."),
	ALREADY_LISTED(HttpStatus.BAD_REQUEST, "이미 등록된 티켓입니다"),
	LISTING_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 리셀을 찾을 수 없습니다"),
	LISTING_ALREADY_CLOSED(HttpStatus.BAD_REQUEST, "리셀 등록 가능한 시간이 지났습니다."),
	NOT_PURCHASABLE(HttpStatus.BAD_REQUEST, "구매할 수 없는 상태입니다"),
	NOT_MATCH_STATUS(HttpStatus.BAD_REQUEST, "{0}상태 에만 할 수 있습니다."),
	TRANSACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "등록된 리셀을 찾을 수 없습니다"),

	AUTH_INVALID_ACCESS_PATH(HttpStatus.UNAUTHORIZED, "올바르지 않은 접근 경로입니다."),
	AUTH_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
	AUTH_INVALID(HttpStatus.UNAUTHORIZED, "올바르지 않은 인증 정보입니다."),
	AUTH_ACCESS_EXPIRED(HttpStatus.UNAUTHORIZED, "엑세스 토큰이 만료되었습니다."),
	AUTH_SIGNUP_EXPIRED(HttpStatus.GONE, "회원가입 유효 시간이 만료되었습니다. 다시 소셜 로그인을 진행해주세요."),
	AUTH_CODE_NOT_FOUND(HttpStatus.UNAUTHORIZED, "인증 시간이 만료되었거나 해당 휴대전화번호로 인증번호 전송 이력이 없습니다."),
	AUTH_CODE_INVALID(HttpStatus.UNAUTHORIZED, "인증 코드가 일치하지 않습니다."),

	GAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 동일한 경기 일정이 존재합니다."),
	GAME_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 경기입니다."),

	SEAT_GRADE_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 동일한 좌석 등급이 존재합니다."),
	SEAT_GRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석 등급 정보를 찾을 수 없습니다."),
	SEAT_GRADE_STADIUM_MISMATCH(HttpStatus.BAD_REQUEST, "좌석 등급이 요청한 구장 정보와 일치하지 않습니다."),
	SEAT_SECTION_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 동일한 좌석 구역이 존재합니다."),
	SEAT_LOCK_ACQUIRE_FAILED(HttpStatus.BAD_REQUEST, "요청이 몰리고 있습니다. 잠시 후 다시 시도해주세요"),
	SEAT_STATUS_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석 상태를 확인할 수 없습니다."),
	SEAT_HOLD_NOT_FOUND(HttpStatus.NOT_FOUND, "좌석 점유 정보를 찾을 수 없습니다."),

	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
	SOCIAL_PROVIDER_ALREADY_LINKED(HttpStatus.BAD_REQUEST, "해당 소셜 계정은 이미 다른 회원과 연동되어 있습니다."),

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생하였습니다. 잠시 후 다시 시도해주세요.");

	private final HttpStatus status;
	private final String message;

	public String format(Object... args) {
		return MessageFormat.format(this.message, args);
	}

	public boolean isSystemError() {
		return status.is5xxServerError();
	}
}
