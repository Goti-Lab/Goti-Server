package seat;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import com.goti.domain.entity.seat.GameSeatInventoryEntity;
import com.goti.exception.FieldValidationException;

@ActiveProfiles("test")
public class GameSeatInventoryEntityTest {

	UUID gameId;
	UUID seatGradeId;
	Integer totalCount;
	Integer availableCount;

	@BeforeEach
	void setup() {
		gameId = UUID.randomUUID();
		seatGradeId = UUID.randomUUID();
		totalCount = 100;
		availableCount = 100;
	}

	@Test
	void 좌석재고요약_생성_성공() {
		GameSeatInventoryEntity inventory = GameSeatInventoryEntity.create(
			gameId,
			seatGradeId,
			totalCount,
			availableCount
		);

		assertNotNull(inventory);
		assertThat(inventory.getGameId()).isEqualTo(gameId);
		assertThat(inventory.getSeatGradeId()).isEqualTo(seatGradeId);
		assertThat(inventory.getTotalCount()).isEqualTo(totalCount);
		assertThat(inventory.getAvailableCount()).isEqualTo(availableCount);
		assertThat(inventory.getHeldCount()).isZero();
		assertThat(inventory.getSoldCount()).isZero();
		assertThat(inventory.getBlockedCount()).isZero();
	}

	@Test
	void 좌석재고요약_생성_성공_경계값_0() {
		GameSeatInventoryEntity inventory = GameSeatInventoryEntity.create(
			gameId,
			seatGradeId,
			0,
			0
		);

		assertThat(inventory.getTotalCount()).isZero();
		assertThat(inventory.getAvailableCount()).isZero();
	}

	@Test
	void 좌석재고요약_생성_실패_경기ID_null() {
		assertThatThrownBy(
			() -> GameSeatInventoryEntity.create(
				null,
				seatGradeId,
				totalCount,
				availableCount
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("경기 ID는 필수입니다.");
	}

	@Test
	void 좌석재고요약_생성_실패_좌석등급ID_null() {
		assertThatThrownBy(
			() -> GameSeatInventoryEntity.create(
				gameId,
				null,
				totalCount,
				availableCount
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("좌석 등급 ID는 필수입니다.");
	}

	@Test
	void 좌석재고요약_생성_실패_전체좌석수_null() {
		assertThatThrownBy(
			() -> GameSeatInventoryEntity.create(
				gameId,
				seatGradeId,
				null,
				availableCount
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("전체 좌석 수는 0 이상이어야 합니다.");
	}

	@Test
	void 좌석재고요약_생성_실패_전체좌석수_음수() {
		assertThatThrownBy(
			() -> GameSeatInventoryEntity.create(
				gameId,
				seatGradeId,
				-1,
				availableCount
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("전체 좌석 수는 0 이상이어야 합니다.");
	}

	@Test
	void 좌석재고요약_생성_실패_잔여좌석수_null() {
		assertThatThrownBy(
			() -> GameSeatInventoryEntity.create(
				gameId,
				seatGradeId,
				totalCount,
				null
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("잔여 좌석 수는 0 이상이어야 합니다.");
	}

	@Test
	void 좌석재고요약_생성_실패_잔여좌석수_음수() {
		assertThatThrownBy(
			() -> GameSeatInventoryEntity.create(
				gameId,
				seatGradeId,
				totalCount,
				-1
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("잔여 좌석 수는 0 이상이어야 합니다.");
	}

	@Test
	void 좌석재고요약_생성_실패_잔여좌석수가_전체보다_큼() {
		assertThatThrownBy(
			() -> GameSeatInventoryEntity.create(
				gameId,
				seatGradeId,
				100,
				101
			)
		).isInstanceOf(FieldValidationException.class)
			.hasMessageContaining("잔여 좌석 수는 전체 좌석 수보다 클 수 없습니다.");
	}
}
