package com.goti.domain.entity.resale;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.exception.FieldValidationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
class ResaleRestrictionEntityTest {

	private static final UUID VALID_USER_ID = UUID.randomUUID();

	@Test
	void 리셀_제한_생성_성공() {
		ResaleRestrictionEntity entity = ResaleRestrictionEntity.create(VALID_USER_ID);

		assertAll(
			() -> assertThat(entity.getUserId()).isEqualTo(VALID_USER_ID),
			() -> assertThat(entity.getDailyBuyCount()).isZero(),
			() -> assertThat(entity.getDailySellCount()).isZero(),
			() -> assertThat(entity.getDailyCancelCount()).isZero(),
			() -> assertThat(entity.getLastBuyAt()).isNull(),
			() -> assertThat(entity.getLastSellAt()).isNull(),
			() -> assertThat(entity.getLastCancelAt()).isNull(),
			() -> assertThat(entity.getResaleBlockedUntil()).isNull()
		);
	}

	@Test
	void 초기_카운트가_모두_0_성공() {
		ResaleRestrictionEntity entity = ResaleRestrictionEntity.create(VALID_USER_ID);

		assertAll(
			() -> assertThat(entity.getDailyBuyCount()).isEqualTo(0),
			() -> assertThat(entity.getDailySellCount()).isEqualTo(0),
			() -> assertThat(entity.getDailyCancelCount()).isEqualTo(0)
		);
	}

	@Test
	void 모든_날짜_필드가_null_성공() {
		ResaleRestrictionEntity entity = ResaleRestrictionEntity.create(VALID_USER_ID);

		assertAll(
			() -> assertThat(entity.getLastBuyAt()).isNull(),
			() -> assertThat(entity.getLastSellAt()).isNull(),
			() -> assertThat(entity.getLastCancelAt()).isNull(),
			() -> assertThat(entity.getResaleBlockedUntil()).isNull()
		);
	}

	@Test
	void 유저_ID가_null_실패() {
		assertThatThrownBy(() -> ResaleRestrictionEntity.create(null))
			.isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("유저 ID는 비어 있을 수 없습니다");
	}
}