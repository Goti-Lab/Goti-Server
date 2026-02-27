package com.goti.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.goti.GotiUserApplication;
import com.goti.infra.constants.ProviderType;

import com.goti.infra.constants.redis.RedisKey;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.infra.api.dto.response.common.SocialStateResponse;
import com.goti.infra.cache.RedisCache;
import com.goti.service.auth.application.AuthApplicationService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(classes = GotiUserApplication.class)
public class AuthApplicationServiceTest {

	@Autowired
	AuthApplicationService authApplicationService;

	@Autowired
	RedisCache redisCache;

	static final String KEY_SEPARATOR = ":";

	@Test
	@DisplayName("method : issueState()")
	void state_생성_성공() {
		ProviderType provider = ProviderType.NAVER;
		SocialStateResponse response = authApplicationService.issueState(provider);
		assertNotNull(response);
		String state = response.state();
		log.info("state :: {}", response.state());
		String keyParam = provider + KEY_SEPARATOR + state;
		boolean isExists = redisCache.get(RedisKey.OAUTH_STATE.getKey(keyParam), Boolean.class);
		assertTrue(isExists);
		log.info("isExists :: {}", isExists);
	}

	@Test
	@DisplayName("method : issueState()")
	void state_생성_실패_kakao() {
		ProviderType provider = ProviderType.KAKAO;
		assertThatThrownBy(
			() -> authApplicationService.issueState(provider)
		).isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.BAD_REQUEST.getMessage());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@DisplayName("method : login() (google, naver)")
	void 엑세스토큰_생성_실패_state_필수_소셜로그_state_null_또는_공백(String code) {
		ProviderType provider = ProviderType.NAVER; // (or GOOGLE)
		assertThatThrownBy(
			() -> authApplicationService.login(provider, code, null)
		).isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.MISSING_PARAMETER.format("state"));
	}

	@Test
	@DisplayName("method : login() (google, naver)")
	void 엑세스토큰_생성_실패_state_필수_소셜로그_redis_state_미존재() {
		ProviderType provider = ProviderType.NAVER; // (or GOOGLE)
		String code = "testCode";
		String state = "NOT_EXISTS_STATE";
		assertThatThrownBy(
			() -> authApplicationService.login(provider, code, state)
		).isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.INVALID_STATE.getMessage());
	}
}
