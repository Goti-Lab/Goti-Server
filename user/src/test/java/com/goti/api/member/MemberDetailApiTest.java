package com.goti.api.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.constants.Gender;
import com.goti.constants.OAuthProvider;
import com.goti.user.GotiUserApplication;

import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.domain.entity.user.SocialProviderEntity;
import com.goti.user.repository.MemberRepository;

import com.goti.user.repository.SocialProviderRepository;

import com.goti.user.security.ExtendedUserDetails;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
@DisplayName("회원 (본인)상세 조회 - Get /api/v1/members/me")
public class MemberDetailApiTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private SocialProviderRepository socialProviderRepository;

	private MemberEntity member;
	private ExtendedUserDetails authenticator;

	private static final String PROVIDER_ID = "google_test_123";
	private static final OAuthProvider PROVIDER = OAuthProvider.GOOGLE;

	@BeforeEach
	void setup() {
		member = MemberEntity.create(
			"01012345678",
			"김고티",
			Gender.MALE,
			LocalDate.of(1995, 5, 5)
		);
		memberRepository.save(member);

		SocialProviderEntity socialProvider = SocialProviderEntity.create(
			member,
			PROVIDER,
			PROVIDER_ID,
			"goti@google.com"
		);
		socialProviderRepository.save(socialProvider);

		authenticator = new ExtendedUserDetails(
			member.getId(),
			member.getRole(),
			PROVIDER_ID,
			PROVIDER
		);
	}

	@Test
	@DisplayName("회원 본인 상세조회 성공")
	void 회원_본인_상세_조회_성공__200_OK() throws Exception {
		MvcResult result = mockMvc.perform(
				get("/api/v1/members/me")
					.with(user(authenticator))
					.contentType(MediaType.APPLICATION_JSON)
			)
			.andDo(print())
			.andExpectAll(
				status().isOk(),
				jsonPath("$.code").value("ok"),
				jsonPath("$.message").value("성공"),
				jsonPath("$.data.name").value(member.getName()),
				jsonPath("$.data.email").value("goti@google.com"),
				jsonPath("$.data.oAuthProvider").value("GOOGLE"),

				// 소셜 연결 정보 검증 (우리가 만든 SocialConnection 로직 확인)
				jsonPath("$.data.socialConnection.isGoogleConnected").value(true),
				jsonPath("$.data.socialConnection.isKakaoConnected").value(false),
				jsonPath("$.data.socialConnection.isNaverConnected").value(false)
			)
			.andReturn();

		String responseJson = result.getResponse().getContentAsString();
		log.info("Detail Profile Response : {}", responseJson);
	}
}
