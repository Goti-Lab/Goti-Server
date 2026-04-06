package domain.account;

import com.goti.constants.Gender;
import com.goti.exception.FieldValidationException;
import com.goti.user.domain.entity.user.AccountEntity;
import com.goti.user.domain.entity.user.MemberEntity;

import com.goti.user.domain.entity.user.SocialProviderEntity;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
public class AccountEntityTest {

	MemberEntity member;

	@BeforeEach
	void setup() {
		member = MemberEntity.create(
			"01012341234",
			"테스트회원",
			Gender.MALE,
			LocalDate.of(2000, 2, 4)
		);
		assertNotNull(member);
	}

	@Test
	void 계좌_생성_성공() {
		AccountEntity accountEntity = AccountEntity.create(
			"1002-876-543210",
			"우리은행",
			"테스트예금주",
			member
		);
		assertNotNull(accountEntity);
		assertEquals("1002-876-543210", accountEntity.getAccountNumber());
		assertEquals("우리은행", accountEntity.getBankName());
		assertEquals("테스트예금주", accountEntity.getAccountHolder());
		assertEquals(member, accountEntity.getMember());
		log.info("account name:: {}", accountEntity.getAccountNumber());
		log.info("account holder:: {}", accountEntity.getAccountHolder());
		log.info("account bankName:: {}", accountEntity.getBankName());
	}


	@ParameterizedTest
	@NullAndEmptySource
	void 계좌_생성_실패_account_number_null_또는_공백(String accountNumber) {
		assertThatThrownBy(
			() -> AccountEntity.create(
				accountNumber,
				"우리은행",
				"테스트예금주",
				member
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("계좌번호는 비어 있을 수 없습니다.");
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 계좌_생성_실패_bank_name_null_또는_공백(String bankName) {
		assertThatThrownBy(
			() -> AccountEntity.create(
				"1002-876-543210",
				bankName,
				"테스트예금주",
				member
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("계좌 은행은 비어 있을 수 없습니다.");
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 계좌_생성_실패_account_holder_null_또는_공백(String accountHolder) {
		assertThatThrownBy(
			() -> AccountEntity.create(
				"1002-876-543210",
				"우리은행",
				accountHolder,
				member
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("계좌 예금주는 비어 있을 수 없습니다.");
	}

	@Test
	void 계좌_생성_실패_member_null() {
		assertThatThrownBy(
			() -> AccountEntity.create(
				"1002-876-543210",
				"우리은행",
				"테스트예금주",
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("회원 정보는 비어 있을 수 없습니다.");
	}





}
