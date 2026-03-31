package com.goti.api.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.constants.Gender;
import com.goti.user.GotiUserApplication;

import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.dto.request.AccountRegisterRequest;
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
@DisplayName("계좌 등록  - Post /api/v1/members/accounts")
public class AccountRegisterApiTest {

	@Autowired
	public MockMvc mockMvc;

	@Autowired
	public ObjectMapper objectMapper;

	@Autowired
	MemberRepository memberRepository;

	MemberEntity member;
	ExtendedUserDetails authenticator;

	@BeforeEach
	void setup() {
		member = MemberEntity.create(
			"01012341234",
			"테스트회원",
			Gender.MALE,
			LocalDate.of(2000, 2, 4)
		);
		memberRepository.save(member);

		authenticator = new ExtendedUserDetails(
			member.getId(),
			member.getMobile(),
			member.getRole()
		);
	}

	@Test
	@DisplayName("계좌 등록 성공")
	void 계좌_등록_성공__200_OK() throws Exception {
		AccountRegisterRequest request = new AccountRegisterRequest(
			"1002-876-543219",
			"우리은행",
			"테스트예금주"
		);

		MvcResult result = mockMvc.perform(
				post("/api/v1/members/accounts")
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
				jsonPath("$.data.accountId").exists(),
				jsonPath("$.data.accountNumber").value(request.accountNumber()),
				jsonPath("$.data.bankName").value(request.bankName()),
				jsonPath("$.data.accountHolder").value(request.accountHolder())
			).andReturn();

		String responseJson = result.getResponse().getContentAsString();
		log.info("response : {}", responseJson);
	}



}
