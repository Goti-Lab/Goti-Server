package com.goti.user.service.auth.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.goti.constants.Gender;
import com.goti.constants.OAuthProvider;
import com.goti.user.GotiUserApplication;

import com.goti.infra.constants.redis.RedisKey;

import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.dto.response.SocialVerifyResponse;

import com.goti.user.repository.MemberRepository;

import com.goti.user.service.application.auth.SocialAuthService;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.infra.api.dto.response.common.SocialStateResponse;
import com.goti.infra.cache.RedisCache;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(classes = GotiUserApplication.class)
public class AuthApplicationServiceTest {

	@Autowired
	SocialAuthService socialAuthService;

	@Autowired
	RedisCache redisCache;

	@Autowired
	MemberRepository memberRepository;

	static final String KEY_SEPARATOR = ":";

	@Test
	@Disabled("개별적으로 테스트 시에만 @Disabled 주석 해제 후 테스트")
	@DisplayName("method: verify()")
	void verify_성공_for_kakao_isRegistered_false_값_기대() {
		// 브라우저 단에서 직접 호출 후 실제 받은 데이터 주입
		String realAuthCode = "";
		SocialVerifyResponse response = socialAuthService.verify(
			OAuthProvider.KAKAO, realAuthCode, null
		);

		assertNotNull(response);
		assertFalse(response.isRegistered());
		log.info("response isRegistered :: {}", response.isRegistered());
	}

	@Test
	@Disabled("개별적으로 테스트 시에만 @Disabled 주석 해제 후 테스트")
	@DisplayName("method: signup()")
	void signup_성공_verify_이후_socialVerifyToken_발급_sms_code_생략() {
		// 브라우저 단에서 직접 호출 후 실제 받은 데이터 주입
		String realAuthCode = "";
		String mobile = "01012341234";
		String smsCode = "123456";
		SocialVerifyResponse response = socialAuthService.verify(
			OAuthProvider.KAKAO, realAuthCode, null
		);
		String verifyToken = response.socialVerifyToken();
		redisCache.set(
			RedisKey.SMS_AUTH_CODE,
			mobile,
			smsCode
		);
		var signResponse = socialAuthService.signup(
			verifyToken,
			"테스트회원",
			mobile,
			Gender.MALE,
			LocalDate.of(2000, 02, 10),
			smsCode
		);
		assertNotNull(signResponse);
		log.info("signupResponse first : {}", signResponse.getFirst());

		MemberEntity member = memberRepository.findByMobile(mobile).orElse(null);
		assertNotNull(member);
		log.info("member id : {}", member.getId());
	}

	@Test
	@Disabled("개별적으로 테스트 시에만 @Disabled 주석 해제 후 테스트")
	@DisplayName("method: login()")
	void login_성공_verify_이후_test_db_signup_사전_회원저장_필수() {
		// 브라우저 단에서 직접 호출 후 실제 받은 데이터 주입
		String realAuthCode = "";
		SocialVerifyResponse verifyResponse = socialAuthService.verify(
			OAuthProvider.KAKAO, realAuthCode, null
		);
		log.info("response isRegistered : {}", verifyResponse.isRegistered());

		String verifyToken = verifyResponse.socialVerifyToken();
		var loginResponse = socialAuthService.login(verifyToken);
		assertNotNull(loginResponse);
		log.info("accessToken : {}", loginResponse.getFirst());
	}

	@Test
	@Disabled("개별적으로 테스트 시에만 @Disabled 주석 해제 후 테스트")
	@DisplayName("Method : reIssueToken()")
	void reIssueToken_성공_로그인_이후_accessToken_refreshToken_재발급() {
		// 브라우저 단에서 직접 호출 후 실제 받은 데이터 주입
		String realAuthCode = "";
		SocialVerifyResponse verifyResponse = socialAuthService.verify(
			OAuthProvider.KAKAO, realAuthCode, null
		);
		log.info("response isRegistered : {}", verifyResponse.isRegistered());

		String verifyToken = verifyResponse.socialVerifyToken();
		var loginResponse = socialAuthService.login(verifyToken);
		String firstAccessToken = loginResponse.getFirst();
		String firstRefreshToken = loginResponse.getSecond();
		var reIssueResponse = socialAuthService.reissueToken(firstRefreshToken);
		assertNotNull(reIssueResponse);
		log.info("firstAccessToken : {}", firstAccessToken);
		log.info("firstRefreshToken : {}", firstRefreshToken);
		String reIssuedAccessToken = reIssueResponse.getFirst();
		String reIssuedRefreshToken = reIssueResponse.getSecond();
		assertNotEquals(firstAccessToken, reIssuedAccessToken);
		assertNotEquals(firstRefreshToken, reIssuedRefreshToken);
		log.info("reIssuedAccessToken : {}", reIssuedAccessToken);
		log.info("reIssuedRefreshToken : {}", reIssuedRefreshToken);
	}


	@Test
	@DisplayName("method : issueState()")
	void state_생성_성공() {
		OAuthProvider provider = OAuthProvider.NAVER;
		SocialStateResponse response = socialAuthService.issueState(provider);
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
			() -> socialAuthService.issueState(provider)
		).isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.BAD_REQUEST.getMessage());
	}

}
