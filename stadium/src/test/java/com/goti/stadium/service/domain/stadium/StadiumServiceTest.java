package com.goti.stadium.service.domain.stadium;

import com.goti.stadium.GotiStadiumApplication;
import com.goti.stadium.dto.request.StadiumCreateRequest;

import com.goti.stadium.dto.response.StadiumCreateResponse;

import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@Transactional
@SpringBootTest(classes = GotiStadiumApplication.class)
@ActiveProfiles("test")
public class StadiumServiceTest {

	@Autowired
	StadiumService stadiumService;

	StadiumCreateRequest stadiumCreateRequest;

	@BeforeEach
	void setup() {
		stadiumCreateRequest = new StadiumCreateRequest(
			"대구삼성라이온즈파크",
			"대구",
			"대구광역시",
			"수성구",
			"대구 수성구 야구전설로 1 대구삼성라이온즈파크",
			BigDecimal.valueOf(35.84),
			BigDecimal.valueOf(128.68),
			24000,
			null // 추후 디테일 정보 기입 예정
		);
	}

	@Test
	void 구장_생성_성공() {

		StadiumCreateResponse response = stadiumService.create(
			stadiumCreateRequest.stadiumName(),
			stadiumCreateRequest.location(),
			stadiumCreateRequest.city(),
			stadiumCreateRequest.district(),
			stadiumCreateRequest.roadAddress(),
			stadiumCreateRequest.latitude(),
			stadiumCreateRequest.longitude(),
			stadiumCreateRequest.totalSeats(),
			stadiumCreateRequest.seatMapConfig()
		);
		assertNotNull(response.stadiumId());
		log.info("response stadiumId :: {}", response.stadiumId());
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 구장_생성_실패_stadiumName__null_또는_공백(String stadiumName) {
		assertThatThrownBy(
			() -> stadiumService.create(
				stadiumName,
				stadiumCreateRequest.location(),
				stadiumCreateRequest.city(),
				stadiumCreateRequest.district(),
				stadiumCreateRequest.roadAddress(),
				stadiumCreateRequest.latitude(),
				stadiumCreateRequest.longitude(),
				stadiumCreateRequest.totalSeats(),
				stadiumCreateRequest.seatMapConfig()
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("StadiumName:Field-stadiumName invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 구장명은 비어 있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 구장_생성_실패_location__null_또는_공백(String location) {
		assertThatThrownBy(
			() -> stadiumService.create(
				stadiumCreateRequest.stadiumName(),
				location,
				stadiumCreateRequest.city(),
				stadiumCreateRequest.district(),
				stadiumCreateRequest.roadAddress(),
				stadiumCreateRequest.latitude(),
				stadiumCreateRequest.longitude(),
				stadiumCreateRequest.totalSeats(),
				stadiumCreateRequest.seatMapConfig()
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("StadiumName:Field-location invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 지역명은 비어 있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 구장_생성_실패_city__null_또는_공백(String city) {
		assertThatThrownBy(
			() -> stadiumService.create(
				stadiumCreateRequest.stadiumName(),
				stadiumCreateRequest.location(),
				city,
				stadiumCreateRequest.district(),
				stadiumCreateRequest.roadAddress(),
				stadiumCreateRequest.latitude(),
				stadiumCreateRequest.longitude(),
				stadiumCreateRequest.totalSeats(),
				stadiumCreateRequest.seatMapConfig()
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("StadiumName:Field-city invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 시/도는 비어 있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 구장_생성_실패_district__null_또는_공백(String district) {
		assertThatThrownBy(
			() -> stadiumService.create(
				stadiumCreateRequest.stadiumName(),
				stadiumCreateRequest.location(),
				stadiumCreateRequest.city(),
				district,
				stadiumCreateRequest.roadAddress(),
				stadiumCreateRequest.latitude(),
				stadiumCreateRequest.longitude(),
				stadiumCreateRequest.totalSeats(),
				stadiumCreateRequest.seatMapConfig()
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("StadiumName:Field-district invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 시/군/구는 비어 있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@ParameterizedTest
	@NullAndEmptySource
	void 구장_생성_실패_roadAddress__null_또는_공백(String roadAddress) {
		assertThatThrownBy(
			() -> stadiumService.create(
				stadiumCreateRequest.stadiumName(),
				stadiumCreateRequest.location(),
				stadiumCreateRequest.city(),
				stadiumCreateRequest.district(),
				roadAddress,
				stadiumCreateRequest.latitude(),
				stadiumCreateRequest.longitude(),
				stadiumCreateRequest.totalSeats(),
				stadiumCreateRequest.seatMapConfig()
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("StadiumName:Field-roadAddress invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 도로명 주소는 비어 있을 수 없습니다.", ex.getMessage());
			}
		);
	}

	@Test
	void 구장_생성_실패_latitude__범위_미포함() {
		assertThatThrownBy(
			() -> stadiumService.create(
				stadiumCreateRequest.stadiumName(),
				stadiumCreateRequest.location(),
				stadiumCreateRequest.city(),
				stadiumCreateRequest.district(),
				stadiumCreateRequest.roadAddress(),
				BigDecimal.valueOf(100),
				stadiumCreateRequest.longitude(),
				stadiumCreateRequest.totalSeats(),
				stadiumCreateRequest.seatMapConfig()
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("StadiumName:Field-latitude invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 위도는 -90에서 90 사이의 값이어야 합니다.", ex.getMessage());
			}
		);
	}

	@Test
	void 구장_생성_실패_longitude__범위_미포함() {
		assertThatThrownBy(
			() -> stadiumService.create(
				stadiumCreateRequest.stadiumName(),
				stadiumCreateRequest.location(),
				stadiumCreateRequest.city(),
				stadiumCreateRequest.district(),
				stadiumCreateRequest.roadAddress(),
				stadiumCreateRequest.latitude(),
				BigDecimal.valueOf(190),
				stadiumCreateRequest.totalSeats(),
				stadiumCreateRequest.seatMapConfig()
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("StadiumName:Field-longitude invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 경도는 -180에서 180 사이의 값이어야 합니다.", ex.getMessage());
			}
		);
	}

	@Test
	void 구장_생성_실패_totalSeats__음수() {
		assertThatThrownBy(
			() -> stadiumService.create(
				stadiumCreateRequest.stadiumName(),
				stadiumCreateRequest.location(),
				stadiumCreateRequest.city(),
				stadiumCreateRequest.district(),
				stadiumCreateRequest.roadAddress(),
				stadiumCreateRequest.latitude(),
				stadiumCreateRequest.longitude(),
				0,
				stadiumCreateRequest.seatMapConfig()
			)
		).isInstanceOfSatisfying(
			FieldValidationException.class, ex -> {
				log.info("StadiumName:Field-totalSeats invalid ErrorMessage : {}", ex.getMessage());
				assertEquals("도메인 필드 오류 : 총 좌석 수는 0보다 커야 합니다.", ex.getMessage());
			}
		);
	}

}
