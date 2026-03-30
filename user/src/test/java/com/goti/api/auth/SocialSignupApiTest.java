package com.goti.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.goti.constants.Gender;
import com.goti.constants.OAuthProvider;
import com.goti.infra.cache.RedisCache;
import com.goti.infra.constants.redis.RedisKey;
import com.goti.user.GotiUserApplication;
import com.goti.user.config.jwt.JwtTokenProvider;
import com.goti.user.dto.request.SignupRequest;

import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest(classes = GotiUserApplication.class)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("회원 가입 및 사용자 식별 - POST /api/v1/auth/signup")
public class SocialSignupApiTest {

	@Autowired
	public MockMvc mockMvc;

	@Autowired
	public ObjectMapper objectMapper;

	@Autowired
	JwtTokenProvider jwtTokenProvider;

	@Autowired
	RedisCache redisCache;

	static final String KAKAO_SOCIAL_PROVIDER_ID = "new_kakao_provider_id";
	static final String EMAIL = "kakao_user@test.com";
	static final String MOBILE = "01012341234";
	static final String AUTH_CODE = "123123";

	@Test
	void signup_성공_및_refresh_token_cookie_저장_200__OK_() throws Exception {
		String socialVerifyToken = jwtTokenProvider.createSocialVerifyToken(
			OAuthProvider.KAKAO, KAKAO_SOCIAL_PROVIDER_ID, EMAIL
		);

		redisCache.set(RedisKey.SMS_AUTH_CODE, MOBILE, AUTH_CODE);

		SignupRequest request = new SignupRequest(
			socialVerifyToken,
			"테스트_회원",
			"01012341234",
			Gender.MALE,
			LocalDate.of(2000, 2, 10),
			AUTH_CODE
		);

		MvcResult result = mockMvc.perform(
				post("/api/v1/auth/signup")
					.with(csrf())
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andDo(print())
			.andExpectAll(
				status().isOk(),
				jsonPath("$.code").value("ok"),
				jsonPath("$.message").value("성공"),
				jsonPath("$.data").exists(),
				jsonPath("$.data.accessToken").exists(),
				header().exists(HttpHeaders.SET_COOKIE),

				cookie().exists("refreshToken"),
				cookie().httpOnly("refreshToken", true),
				cookie().secure("refreshToken", false)
			).andReturn();

		String responseJson = result.getResponse().getContentAsString();
		log.info("response : {}", responseJson);

		String setCookieHeader = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
		log.info("Set-Cookie Header: {}", setCookieHeader);

		Cookie refreshTokenCookie = result.getResponse().getCookie("refreshToken");
		if (refreshTokenCookie != null) {
			log.info("Refresh Token in Cookie: {}", refreshTokenCookie.getValue());
		}
	}
}
