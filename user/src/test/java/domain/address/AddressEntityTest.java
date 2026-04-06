package domain.address;

import com.goti.constants.Gender;
import com.goti.exception.FieldValidationException;
import com.goti.user.domain.entity.user.AddressEntity;
import com.goti.user.domain.entity.user.MemberEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
public class AddressEntityTest {
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
	void 주소_정보_생성_성공() {
		AddressEntity address = AddressEntity.create(
			"06111",
			"서울특별시 강남구 학동로 343",
			"(논현동, 포바강남타워) 4층, 15층",
			member
		);
		assertNotNull(address);
		assertEquals("06111", address.getZipCode());
		assertEquals("서울특별시 강남구 학동로 343", address.getBaseAddress());
		assertEquals("(논현동, 포바강남타워) 4층, 15층", address.getDetailAddress());
		assertEquals(member, address.getMember());
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 주소_생성_실패_zipCode_null_또는_공백(String zipCode) {
		assertThatThrownBy(
			() -> AddressEntity.create(
				zipCode,
				"서울특별시 강남구 학동로 343",
				"(논현동, 포바강남타워) 4층, 15층",
				member
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("우편번호는 비어있을 수 없습니다.");
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 주소_생성_실패_baseAddress_null_또는_공백(String baseAddress) {
		assertThatThrownBy(
			() -> AddressEntity.create(
				"06111",
				baseAddress,
				"(논현동, 포바강남타워) 4층, 15층",
				member
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("기본 주소는 비어있을 수 없습니다.");
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 주소_생성_실패_detailAddress_null_또는_공백(String detailAddress) {
		assertThatThrownBy(
			() -> AddressEntity.create(
				"06111",
				"서울특별시 강남구 학동로 343",
				detailAddress,
				member
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("상세 주소는 비어있을 수 없습니다.");
	}

	@Test
	void 주소_생성_실패_member_null() {
		assertThatThrownBy(
			() -> AddressEntity.create(
				"06111",
				"서울특별시 강남구 학동로 343",
				"(논현동, 포바강남타워) 4층, 15층",
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("회원 정보는 비어있을 수 없습니다.");
	}

}
