package user;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.Gender;
import com.goti.user.domain.entity.user.MemberEntity;
import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
public class MemberEntityTest {

	static final String MOBILE = "01012345678";
	static final String NAME = "테스트회원";
	MemberEntity member;

	@Test
	void 회원_도메인_생성_성공() {
		member = MemberEntity.create(
			"01012341234",
			"테스트회원",
			Gender.MALE,
			LocalDate.of(2026, 2, 4)
		);
		assertNotNull(member);

	}

	@ParameterizedTest
	@NullAndEmptySource
	void 회원_도메인_생성_실패_휴대전화_null_또는_공백(String mobile) {
		assertThatThrownBy(
			() -> MemberEntity.create(mobile, NAME, Gender.MALE, LocalDate.now())
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("회원 휴대전화 번호는 비어 있을 수 없습니다.");
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 회원_도메인_생성_실패_이름_null_또는_공백(String name) {
		assertThatThrownBy(
			() -> MemberEntity.create(MOBILE, name, Gender.MALE, LocalDate.now())
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("회원 이름은 비어 있을 수 없습니다.");
	}

	@Test
	void 회원_도메인_생성_실패_성별_null() {
		assertThatThrownBy(
			() -> MemberEntity.create(MOBILE, NAME, null, LocalDate.now())
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("회원 성별은 비어 있을 수 없습니다.");
	}

	@Test
	void 회원_도메인_생성_실패_생일_null() {
		assertThatThrownBy(
			() -> MemberEntity.create(MOBILE, NAME, Gender.MALE, null)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("회원 생년월일은 비어 있을 수 없습니다.");
	}

	@Test
	void 회원_도메인_생성_실패_생일_미래() {
		assertThatThrownBy(
			() -> MemberEntity.create(MOBILE, NAME, Gender.MALE, LocalDate.now().plusDays(1))
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("회원 생년월일은 과거 날짜여야 합니다.");
	}

	@Test
	void 회원_도메인_생성_실패_생일_오늘() {
		assertThatThrownBy(
			() -> MemberEntity.create(MOBILE, NAME, Gender.MALE, LocalDate.now())
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("회원 생년월일은 과거 날짜여야 합니다.");
	}

}