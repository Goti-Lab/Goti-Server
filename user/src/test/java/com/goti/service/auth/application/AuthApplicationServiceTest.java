package com.goti.service.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.goti.constants.OAuthProvider;
import com.goti.GotiUserApplication;

import com.goti.infra.constants.redis.RedisKey;

import org.junit.jupiter.api.Disabled;
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

	// todo : socialUserInfo 에서 받은 providerId 로 socialProvider 데이터 유무체크
	@Test
	@Disabled("개별적으로 테스트 시에만 @Disabled 주석 해제 후 테스트")
	@DisplayName("method : login()")
	void 카카오_로그인_성공() {
		// 브라우저에서 직접 호출하여 응답받은 code 직접 기입 후 테스트
		String code = "";
		authApplicationService.login(
			OAuthProvider.KAKAO, code, null
		);
	}

	// todo : socialUserInfo 에서 받은 providerId 로 socialProvider 데이터 유무체크
	@Test
	@DisplayName("method : login()")
	@Disabled("개별적으로 테스트 시에만 @Disabled 주석 해제 후 테스트")
	void 네이버_로그인_성공() {
		// 브라우저에서 직접 호출하여 응답받은 code
		// springServer (issueState API) 에서 받은 state 직접 기입 후 테스트
		String code = "";
		String state = "";
		authApplicationService.login(
			OAuthProvider.NAVER, code, state
		);
	}

	// todo : socialUserInfo 에서 받은 providerId 로 socialProvider 데이터 유무체크
	@Test
	@DisplayName("method : login()")
	@Disabled("개별적으로 테스트 시에만 @Disabled 주석 해제 후 테스트")
	void 구글_로그인_성공() {
		// 브라우저에서 직접 호출하여 응답받은 code
		// springServer (issueState API) 에서 받은 state 직접 기입 후 테스트
		String code = "";
		String state = "";
		authApplicationService.login(
			OAuthProvider.GOOGLE, code, state
		);
	}

	@Test
	@DisplayName("method : issueState()")
	void state_생성_성공() {
		OAuthProvider provider = OAuthProvider.NAVER;
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
		OAuthProvider provider = OAuthProvider.KAKAO;
		assertThatThrownBy(
			() -> authApplicationService.issueState(provider)
		).isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.BAD_REQUEST.getMessage());
	}

	@ParameterizedTest
	@NullAndEmptySource
	@DisplayName("method : login() (google, naver)")
	void 엑세스토큰_생성_실패_state_필수_소셜로그_state_null_또는_공백(String code) {
		OAuthProvider provider = OAuthProvider.NAVER; // (or GOOGLE)
		assertThatThrownBy(
			() -> authApplicationService.login(provider, code, null)
		).isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.MISSING_PARAMETER.format("state"));
	}

	@Test
	@DisplayName("method : login() (google, naver)")
	void 엑세스토큰_생성_실패_state_필수_소셜로그_redis_state_미존재() {
		OAuthProvider provider = OAuthProvider.NAVER; // (or GOOGLE)
		String code = "testCode";
		String state = "NOT_EXISTS_STATE";
		assertThatThrownBy(
			() -> authApplicationService.login(provider, code, state)
		).isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.INVALID_STATE.getMessage());
	}
}
