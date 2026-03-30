package com.goti.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.constants.Gender;
import com.goti.constants.OAuthProvider;
import com.goti.infra.cache.RedisCache;
import com.goti.infra.constants.redis.RedisKey;
import com.goti.user.GotiUserApplication;

import com.goti.user.config.jwt.JwtTokenProvider;

import com.goti.user.constants.TokenType;
import com.goti.user.domain.entity.user.MemberEntity;

import com.goti.user.domain.entity.user.SocialProviderEntity;

import com.goti.user.repository.MemberRepository;

import com.goti.user.repository.SocialProviderRepository;

import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Slf4j
@SpringBootTest(classes = GotiUserApplication.class)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("토큰 재발급 - POST /api/v1/auth/reissue")
public class SocialAuthReissueTokenApiTest {

	@Autowired MockMvc mockMvc;
	@Autowired ObjectMapper objectMapper;
	@Autowired JwtTokenProvider jwtTokenProvider;
	@Autowired RedisCache redisCache;
	@Autowired MemberRepository memberRepository;
	@Autowired SocialProviderRepository socialProviderRepository;

	MemberEntity member;
	SocialProviderEntity socialProvider;

	@BeforeEach
	void setup() {
		saveMember();
		saveSocialProvider();
	}

	@Test
	void 토큰_재발급_성공__200_OK() throws Exception {
		String loginAccessToken = jwtTokenProvider.create(
			member.getId(),
			member.getMobile(),
			member.getRole(),
			TokenType.ACCESS
		);
		String loginRefreshToken = jwtTokenProvider.create(
			member.getId(),
			member.getMobile(),
			member.getRole(),
			TokenType.REFRESH
		);

		log.info("login AccessToken: {}", loginAccessToken);
		log.info("login RefreshToken: {}", loginRefreshToken);

		String jti = jwtTokenProvider.extractJti(loginRefreshToken);
		redisCache.set(RedisKey.REFRESH_TOKEN, member.getId(), jti);

		mockMvc.perform(post("/api/v1/auth/reissue")
				.cookie(new Cookie("refreshToken", loginRefreshToken))
				.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.accessToken").exists())
			.andExpect(header().exists(HttpHeaders.SET_COOKIE))
			.andDo(print());
	}

	private void saveMember() {
		member = MemberEntity.create(
			"01012341234",
			"테스트_회원",
			Gender.MALE,
			LocalDate.of(2000,2,10)
		);
		memberRepository.save(member);
	}

	private void saveSocialProvider() {
		socialProvider = SocialProviderEntity.create(
			member,
			OAuthProvider.KAKAO,
			"existing_kakao_provider_id",
			"kakao_user@test.com"
		);
		socialProviderRepository.save(socialProvider);
	}

}