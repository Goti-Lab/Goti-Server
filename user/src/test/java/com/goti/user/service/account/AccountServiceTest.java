package com.goti.user.service.account;

import com.goti.constants.Gender;
import com.goti.user.GotiUserApplication;

import com.goti.user.domain.entity.user.AccountEntity;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.user.dto.response.AccountCreateResponse;
import com.goti.user.repository.AccountRepository;
import com.goti.user.repository.MemberRepository;
import com.goti.user.service.domain.account.AccountService;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@Transactional
@SpringBootTest(classes = GotiUserApplication.class)
@ActiveProfiles("test")
public class AccountServiceTest {

	@Autowired
	AccountService accountService;

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	AccountRepository accountRepository;

	MemberEntity member;

	@BeforeEach
	void setup() {
		member = MemberEntity.create(
			"01012341234",
			"테스트회원",
			Gender.MALE,
			LocalDate.of(2026, 2, 4)
		);
		memberRepository.save(member);
	}

	@Test
	void 계좌_생성_성공() {
		AccountCreateResponse response = accountService.create(
			"1002-876-543210",
			"우리은행",
			"테스트예금주",
			member
		);
		assertNotNull(response);
		AccountEntity account = accountRepository.findByMember(member).orElse(null);
		assertNotNull(account);
		log.info("AccountCreateResponse: accountId :: {}", response.accountId());
		log.info("account: accountId :: {}", account.getId());
	}

	@Test
	void 계좌_생성_성공_기존_계좌_존재() {
		String updateAccountNumber = "1002-876-543211";
		String updateAccountHolder = "테스트예금주1";
		AccountEntity account = AccountEntity.create(
			"1002-876-543210",
			"우리은행",
			"테스트예금주",
			member
		);
		accountRepository.save(account);

		AccountCreateResponse response = accountService.create(
			updateAccountNumber,
			"우리은행",
			updateAccountHolder,
			member
		);
		assertNotNull(response);
		assertEquals(account.getId(), response.accountId());
		assertEquals(account.getAccountNumber(), updateAccountNumber);
		assertEquals(account.getAccountHolder(), updateAccountHolder);

		log.info("AccountCreateResponse: accountId :: {}", response.accountId());
		log.info("account: accountId :: {}", account.getId());
		log.info("account accountNumber :: {}", account.getAccountNumber());
		log.info("account accountHolder :: {}", account.getAccountHolder());
	}


}
