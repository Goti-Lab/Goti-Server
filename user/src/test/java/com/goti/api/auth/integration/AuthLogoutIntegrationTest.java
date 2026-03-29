package com.goti.api.auth.integration;

import com.goti.constants.Gender;
import com.goti.infra.cache.RedisCache;
import com.goti.infra.constants.redis.RedisKey;
import com.goti.user.GotiUserApplication;
import com.goti.user.config.jwt.JwtTokenProvider;

import com.goti.user.constants.TokenType;
import com.goti.user.domain.entity.user.MemberEntity;

import com.goti.user.repository.MemberRepository;

import jakarta.servlet.http.Cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = GotiUserApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthLogoutIntegrationTest {
	@Autowired private MockMvc mockMvc;
	@Autowired private JwtTokenProvider jwtTokenProvider;
	@Autowired private RedisCache redisCache;
	@Autowired private MemberRepository memberRepository;

	MemberEntity member;
	@BeforeEach
	void setup() {
		member = MemberEntity.create(
			"01012341234",
			"테스트회원",
			Gender.MALE,
			LocalDate.of(2000,2,10)
		);
		memberRepository.save(member);
	}

	@RestController
	static class TestController {
		@GetMapping("/api/v1/test/auth-check")
		public String authCheck() { return "ok"; }
	}

	@Test
	void 로그아웃_통합_테스트() throws Exception {
		UUID memberId = member.getId();
		String accessToken = jwtTokenProvider.create(
			member.getId(),
			member.getMobile(),
			member.getRole(),
			TokenType.ACCESS
		);
		String refreshToken = jwtTokenProvider.create(
			member.getId(),
			member.getMobile(),
			member.getRole(),
			TokenType.REFRESH
		);

		String jti = jwtTokenProvider.extractJti(refreshToken);
		redisCache.set(RedisKey.REFRESH_TOKEN, memberId, jti);

		MvcResult logoutResult = mockMvc.perform(post("/api/v1/auth/logout")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.cookie(new Cookie("refreshToken", refreshToken)))
			.andExpect(status().isOk())
			.andReturn();

		Cookie responseCookie = logoutResult.getResponse().getCookie("refreshToken");
		assertThat(responseCookie).isNotNull();
		assertThat(responseCookie.getValue()).isEmpty();
		assertThat(responseCookie.getMaxAge()).isZero();

		String blacklistKey = RedisKey.BLACKLIST.getKey(accessToken);
		assertThat(redisCache.hasKey(blacklistKey)).isTrue();

		assertThat(redisCache.hasKey(RedisKey.REFRESH_TOKEN.getKey(refreshToken))).isFalse();

		mockMvc.perform(get("/api/v1/test/auth-check")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("AUTH_LOGOUT_TOKEN"));
	}

}
