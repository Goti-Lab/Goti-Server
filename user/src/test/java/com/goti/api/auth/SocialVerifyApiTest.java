package com.goti.api.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.constants.Gender;
import com.goti.constants.OAuthProvider;
import com.goti.infra.api.client.SocialClientProvider;
import com.goti.infra.api.client.kakao.KakaoApiClient;
import com.goti.infra.api.dto.response.common.SocialUserInfoResponse;
import com.goti.user.GotiUserApplication;

import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.domain.entity.user.SocialProviderEntity;
import com.goti.user.dto.request.SocialVerifyRequest;

import com.goti.user.repository.MemberRepository;

import com.goti.user.repository.SocialProviderRepository;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Slf4j
@SpringBootTest(classes = GotiUserApplication.class)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("소셜 코드 검증 및 임시토큰 발급 - POST /api/v1/auth/{provider}/social/verify")
public class SocialVerifyApiTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	@MockitoBean
	SocialClientProvider socialClientProvider;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	SocialProviderRepository socialProviderRepository;

	MemberEntity member;
	SocialProviderEntity socialProvider;

	@Test
	@Disabled("개별적으로 테스트 시에만 @Disabled 주석 해제 후 테스트")
	@DisplayName("소셜정보 미존재")
	void verify_성공_200__OK_for_kakao_소셜_정보_미존재() throws Exception {
		// 실제 브라우저단에서 code 발급 후 실제 code 정의
		String realAuthCode = "";
		SocialVerifyRequest request = new SocialVerifyRequest(
			realAuthCode,
			null
		);
		MvcResult result = mockMvc.perform(
				post("/api/v1/auth/{provider}/social/verify", OAuthProvider.KAKAO)
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
				jsonPath("$.data.isRegistered").exists(),
				jsonPath("$.data.isRegistered").value(false),
				jsonPath("$.data.socialVerifyToken").exists()
			).andReturn();

		String responseJson = result.getResponse().getContentAsString();
		log.info("response : {}", responseJson);
	}

	@Nested
	@DisplayName("소셜 정보가 존재하는 경우")
	class WithExistingSocialProvider {

		@BeforeEach
		void init() {
			saveMember();
			saveSocialProvider();
		}

		@Test
		void verify_성공__200__OK_for_kakao_소셜_정보_존재() throws Exception {
			KakaoApiClient mockKakaoClient = mock(KakaoApiClient.class);
			given(socialClientProvider.getClient(OAuthProvider.KAKAO)).willReturn(mockKakaoClient);

			given(mockKakaoClient.getAccessToken(anyString(), any()))
				.willReturn("test_access_token");

			SocialUserInfoResponse mockResponse = new SocialUserInfoResponse(
				socialProvider.getProviderId(),
				member.getName(),
				socialProvider.getEmail(),
				member.getMobile(),
				member.getBirthDate().toString(),
				Gender.MALE
			);

			given(mockKakaoClient.getSocialUserInfo(anyString()))
				.willReturn(mockResponse);

			SocialVerifyRequest request = new SocialVerifyRequest("test_code", null);

			MvcResult result = mockMvc.perform(
					post("/api/v1/auth/{provider}/social/verify", OAuthProvider.KAKAO)
						.with(csrf())
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request))
				).andDo(print())
				.andExpectAll(
					status().isOk(),
					jsonPath("$.code").value("ok"),
					jsonPath("$.message").value("성공"),
					jsonPath("$.data").exists(),
					jsonPath("$.data.isRegistered").exists(),
					jsonPath("$.data.isRegistered").value(true),
					jsonPath("$.data.socialVerifyToken").exists()
				).andReturn();

			String responseJson = result.getResponse().getContentAsString();
			log.info("response : {}", responseJson);
		}

		private void saveMember() {
			member = MemberEntity.create(
				"01012341234",
				"테스트_회원",
				Gender.MALE,
				LocalDate.of(2000,02,10)
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

}
