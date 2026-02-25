package user;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.test.context.ActiveProfiles;

import com.goti.constants.Gender;
import com.goti.constants.OAuthProvider;
import com.goti.domain.entity.user.MemberEntity;
import com.goti.domain.entity.user.SocialProviderEntity;
import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
public class SocialProviderEntityTest {

	OAuthProvider PROVIDER = OAuthProvider.KAKAO;
	String PROVIDER_ID = "TestProviderID";
	String EMAIL = "test@test.com";
	MemberEntity member;

	@BeforeEach
	void setup() {
		String mobile = "01012345678";
		String name = "테스트회원";
		member = MemberEntity.create(
			mobile,
			name,
			Gender.MALE,
			LocalDate.of(2000, 2,2)
		);
	}

	@Test
	void 소셜_제공자_생성_성공() {
		SocialProviderEntity socialProvider = SocialProviderEntity.create(
			member, PROVIDER, PROVIDER_ID, EMAIL
		);
		assertNotNull(socialProvider);
		log.info("social email : {}", socialProvider.getEmail());
		log.info("socialProvider's member name :: {}", socialProvider.getMember().getName());
	}

	@Test
	void 소셜_제공자_생성_실패_provider_null() {
		assertThatThrownBy(
			() -> SocialProviderEntity.create(member, null, PROVIDER_ID, EMAIL)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("소셜 제공자(kakao, naver, google)는 비어 있을 수 없습니다.");
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 소셜_제공자_생성_실패_provider_id_null_또는_공백(String providerId) {
		assertThatThrownBy(
			() -> SocialProviderEntity.create(member, PROVIDER, providerId, EMAIL)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("사용자 소셜 고유 ID 값은 비어있을 수 없습니다.");
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 소셜_제공자_생성_실패_email_null_또는_공백(String email) {
		assertThatThrownBy(
			() -> SocialProviderEntity.create(member, PROVIDER, PROVIDER_ID, email)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("이메일값은 비어있을 수 없습니다.");
	}

}
