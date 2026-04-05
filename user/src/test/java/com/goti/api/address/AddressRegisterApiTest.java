package com.goti.api.address;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.goti.constants.Gender;
import com.goti.constants.OAuthProvider;
import com.goti.user.GotiUserApplication;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.dto.request.AddressRegisterRequest;
import com.goti.user.repository.MemberRepository;

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
@DisplayName("회원 주소 등록 API - POST /api/v1/members/addresses")
class AddressRegisterApiTest {

	@Autowired MockMvc mockMvc;

	@Autowired ObjectMapper objectMapper;

	@Autowired MemberRepository memberRepository;

	MemberEntity member;
	ExtendedUserDetails authenticator;
	private final static String PROVIDER_ID = "test_provider_id";
	private final static OAuthProvider PROVIDER = OAuthProvider.GOOGLE;
	@BeforeEach
	void setup() {

		member = MemberEntity.create(
			"01012341234",
			"테스트회원",
			Gender.FEMALE,
			LocalDate.of(2000, 2, 10)
		);
		memberRepository.save(member);

		authenticator = new ExtendedUserDetails(
			member.getId(),
			member.getRole(),
			PROVIDER_ID,
			PROVIDER
		);
	}

	@Test
	void 주소_등록_성공__200_OK() throws Exception {
		AddressRegisterRequest request = new AddressRegisterRequest(
			"06111",
			"서울특별시 강남구 학동로 343",
			"(논현동, 포바강남타워) 4층"
		);

		MvcResult result = mockMvc.perform(
				post("/api/v1/members/addresses")
					.with(user(authenticator))
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))
			)
			.andDo(print())
			.andExpectAll(
				status().isOk(),
				jsonPath("$.code").value("ok"),
				jsonPath("$.message").value("성공"),
				jsonPath("$.data").exists(),
				jsonPath("$.data.addressId").exists(),
				jsonPath("$.data.zipCode").value(request.zipCode()),
				jsonPath("$.data.baseAddress").value(request.baseAddress()),
				jsonPath("$.data.detailAddress").value(request.detailAddress())
			).andReturn();

		String responseJson = result.getResponse().getContentAsString();
		log.info("response : {}", responseJson);
	}
}
