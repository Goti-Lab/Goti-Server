package stadium;

import com.goti.stadium.constants.TeamCode;
import com.goti.stadium.domain.entity.team.BaseballTeamEntity;

import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
public class BaseballTeamEntityTest {

	BaseballTeamEntity baseballTeam;

	@Test
	void 구단_생성_성공() {
		baseballTeam = BaseballTeamEntity.create(
			TeamCode.SS,
			"삼성",
			"삼성라이온즈",
			"Samsung Lions",
			"삼성",
			"대구",
			1982,
			"대구광역시 수성구 야구전설로 1",
			"42250",
			"https://www.samsunglions.com/",
			"홍길동",
			"삼성그룹",
			"이재용",
			"이종열",
			"박진만",
			"https://image.url/logo.png"
		);
		assertNotNull(baseballTeam);
		log.info("baseballTeam teamName :: {}", baseballTeam.getTeamName());
	}

	@Test
	void 구단_생성_실패_teamCode_null() {
		assertThatThrownBy(
			() -> BaseballTeamEntity.create(
				null,
				"삼성",
				"삼성라이온즈",
				"Samsung Lions",
				"삼성",
				"대구",
				1982,
				"대구광역시 수성구 야구전설로 1",
				"42250",
				"https://www.samsunglions.com/",
				"홍길동",
				"삼성그룹",
				"이재용",
				"이종열",
				"박진만",
				"https://image.url/logo.png"
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("BaseballTeamEntity:Field-teamCode invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 구단(팀)코드는 비어있을 수 없습니다.", ex.getMessage());
			}
		);
	}
	@NullAndEmptySource
	@ParameterizedTest
	void 구단_생성_실패_displayName_null_또는_공백(String displayName) {
		assertThatThrownBy(
			() -> BaseballTeamEntity.create(
				TeamCode.SS,
				displayName,
				"삼성라이온즈",
				"Samsung Lions",
				"삼성",
				"대구",
				1982,
				"대구광역시 수성구 야구전설로 1",
				"42250",
				"https://www.samsunglions.com/",
				"홍길동",
				"삼성그룹",
				"이재용",
				"이종열",
				"박진만",
				"https://image.url/logo.png"
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("BaseballTeamEntity:Field-displayName invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 구단(팀) 표시명은 비어있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구단_생성_실패_teamName_null_또는_공백(String teamName) {
		assertThatThrownBy(
			() -> BaseballTeamEntity.create(
				TeamCode.SS,
				"삼성",
				teamName,
				"Samsung Lions",
				"삼성",
				"대구",
				1982,
				"대구광역시 수성구 야구전설로 1",
				"42250",
				"https://www.samsunglions.com/",
				"홍길동",
				"삼성그룹",
				"이재용",
				"이종열",
				"박진만",
				"https://image.url/logo.png"
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("BaseballTeamEntity:Field-teamName invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 구단(팀)명은 비어있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구단_생성_실패_teamNameEn_null_또는_공백(String teamNameEn) {
		assertThatThrownBy(
			() -> BaseballTeamEntity.create(
				TeamCode.SS,
				"삼성",
				"삼성라이온즈",
				teamNameEn,
				"삼성",
				"대구",
				1982,
				"대구광역시 수성구 야구전설로 1",
				"42250",
				"https://www.samsunglions.com/",
				"홍길동",
				"삼성그룹",
				"이재용",
				"이종열",
				"박진만",
				"https://image.url/logo.png"
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("BaseballTeamEntity:Field-teamNameEn invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 구단(팀) 영문명은 비어있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구단_생성_실패_sponsor_null_또는_공백(String sponsor) {
		assertThatThrownBy(
			() -> BaseballTeamEntity.create(
				TeamCode.SS,
				"삼성",
				"삼성라이온즈",
				"Samsung Lions",
				sponsor,
				"대구",
				1982,
				"대구광역시 수성구 야구전설로 1",
				"42250",
				"https://www.samsunglions.com/",
				"홍길동",
				"삼성그룹",
				"이재용",
				"이종열",
				"박진만",
				"https://image.url/logo.png"
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("BaseballTeamEntity:Field-sponsor invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 스폰서는 비어있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구단_생성_실패_homeGround_null_또는_공백(String homeGround) {
		assertThatThrownBy(
			() -> BaseballTeamEntity.create(
				TeamCode.SS,
				"삼성",
				"삼성라이온즈",
				"Samsung Lions",
				"삼성",
				homeGround,
				1982,
				"대구광역시 수성구 야구전설로 1",
				"42250",
				"https://www.samsunglions.com/",
				"홍길동",
				"삼성그룹",
				"이재용",
				"이종열",
				"박진만",
				"https://image.url/logo.png"
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("BaseballTeamEntity:Field-homeGround invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 연고지는 비어있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@ParameterizedTest
	@NullSource
	@MethodSource("invalidYears")
	void 구단_생성_실패_foundedYear_유효하지_않음(Integer foundedYear) {
		assertThatThrownBy(() ->
			BaseballTeamEntity.create(
				TeamCode.SS,
				"삼성",
				"삼성라이온즈",
				"Samsung Lions",
				"삼성",
				"대구",
				null,
				"대구광역시 수성구 야구전설로 1",
				"42250",
				"https://www.samsunglions.com/",
				"홍길동",
				"삼성그룹",
				"이재용",
				"이종열",
				"박진만",
				"https://image.url/logo.png"
			)
		).isInstanceOfSatisfying(FieldValidationException.class, ex -> {
			log.info("BaseballTeamEntity:Field-foundedYear invalid ErrorMessage : {}", ex.getMessage());
			assertEquals("도메인 필드 오류 : 창단년도는 비어있거나 현재연도와 같거나 미래일 수 없습니다.", ex.getMessage());
		});
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구단_생성_실패_officeAddress_null_또는_공백(String officeAddress) {
		assertThatThrownBy(
			() -> BaseballTeamEntity.create(
				TeamCode.SS,
				"삼성",
				"삼성라이온즈",
				"Samsung Lions",
				"삼성",
				"대구",
				1982,
				officeAddress,
				"42250",
				"https://www.samsunglions.com/",
				"홍길동",
				"삼성그룹",
				"이재용",
				"이종열",
				"박진만",
				"https://image.url/logo.png"
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("BaseballTeamEntity:Field-officeAddress invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 구단 사무실 도로명주소는 비어있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구단_생성_실패_zipCode_null_또는_공백(String zipCode) {
		assertThatThrownBy(
			() -> BaseballTeamEntity.create(
				TeamCode.SS,
				"삼성",
				"삼성라이온즈",
				"Samsung Lions",
				"삼성",
				"대구",
				1982,
				"대구광역시 수성구 야구전설로 1",
				zipCode,
				"https://www.samsunglions.com/",
				"홍길동",
				"삼성그룹",
				"이재용",
				"이종열",
				"박진만",
				"https://image.url/logo.png"
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("BaseballTeamEntity:Field-zipCode invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 구단 사무실 우편주소는 비어있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구단_생성_실패_siteAddress_null_또는_공백(String siteAddress) {
		assertThatThrownBy(
			() -> BaseballTeamEntity.create(
				TeamCode.SS,
				"삼성",
				"삼성라이온즈",
				"Samsung Lions",
				"삼성",
				"대구",
				1982,
				"대구광역시 수성구 야구전설로 1",
				"42250",
				siteAddress,
				"이재용",
				"삼성그룹",
				"홍길동",
				"이종열",
				"박진만",
				"https://image.url/logo.png"
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("BaseballTeamEntity:Field-siteAddress invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 구단 사이트 주소는 비어있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구단_생성_실패_owner_null_또는_공백(String owner) {
		assertThatThrownBy(
			() -> BaseballTeamEntity.create(
				TeamCode.SS,
				"삼성",
				"삼성라이온즈",
				"Samsung Lions",
				"삼성",
				"대구",
				1982,
				"대구광역시 수성구 야구전설로 1",
				"42250",
				"https://www.samsunglions.com/",
				owner,
				"삼성그룹",
				"이재용",
				"이종열",
				"박진만",
				"https://image.url/logo.png"
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("BaseballTeamEntity:Field-owner invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 구단주명은 비어있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구단_생성_실패_generalManager_null_또는_공백(String generalManager) {
		assertThatThrownBy(
			() -> BaseballTeamEntity.create(
				TeamCode.SS,
				"삼성",
				"삼성라이온즈",
				"Samsung Lions",
				"삼성",
				"대구",
				1982,
				"대구광역시 수성구 야구전설로 1",
				"42250",
				"https://www.samsunglions.com/",
				"이재용",
				"삼성그룹",
				"홍길동",
				generalManager,
				"박진만",
				"https://image.url/logo.png"
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("BaseballTeamEntity:Field-generalManager invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 단장명은 비어있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@NullAndEmptySource
	@ParameterizedTest
	void 구단_생성_실패_director_null_또는_공백(String director) {
		assertThatThrownBy(
			() -> BaseballTeamEntity.create(
				TeamCode.SS,
				"삼성",
				"삼성라이온즈",
				"Samsung Lions",
				"삼성",
				"대구",
				1982,
				"대구광역시 수성구 야구전설로 1",
				"42250",
				"https://www.samsunglions.com/",
				"홍길동",
				"삼성그룹",
				"박진만",
				"이종열",
				director,
				"https://image.url/logo.png"
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("BaseballTeamEntity:Field-director invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 감독명은 비어있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	static Stream<Integer> invalidYears() {
		return Stream.of(LocalDate.now().getYear() + 1);
	}
}
