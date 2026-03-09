package com.goti.game.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.goti.constants.LeagueType;
import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.game.BaseballGameStatusEntity;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.exception.CustomException;
import com.goti.game.dto.response.GameResponse;
import com.goti.game.repository.BaseballGameStatusRepository;
import com.goti.game.repository.GameScheduleRepository;
import com.goti.game.service.command.CreateGameCommand;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class GameScheduleApplicationServiceTest {

	@Mock
	private GameScheduleRepository gameScheduleRepository;

	@Mock
	private BaseballGameStatusRepository baseballGameStatusRepository;

	@InjectMocks
	private GameScheduleService baseballGameApplicationService;

	private CreateGameCommand createGameCommand;

	@BeforeEach
	void setUp() {
		createGameCommand = new CreateGameCommand(
			UUID.randomUUID(),
			UUID.randomUUID(),
			UUID.randomUUID(),
			LocalDate.of(2026, 4, 10),
			LocalTime.of(18, 30),
			LeagueType.REGULAR
		);
	}

	@Test
	@DisplayName("method: create() - 경기 생성 성공")
	void 경기_생성_성공() {
		given(gameScheduleRepository.existsByHomeTeamIdAndAwayTeamIdAndPlayDateAndStartAt(
			any(UUID.class), any(UUID.class), any(LocalDate.class), any(LocalTime.class)
		)).willReturn(false);

		GameScheduleEntity savedGame = GameScheduleEntity.create(
			createGameCommand.homeTeamId(),
			createGameCommand.awayTeamId(),
			createGameCommand.stadiumId(),
			createGameCommand.playDate(),
			createGameCommand.startAt()
		);
		ReflectionTestUtils.setField(savedGame, "id", UUID.randomUUID());

		given(gameScheduleRepository.save(any(GameScheduleEntity.class))).willReturn(savedGame);
		given(baseballGameStatusRepository.save(any(BaseballGameStatusEntity.class)))
			.willAnswer(invocation -> invocation.getArgument(0));

		GameResponse response = baseballGameApplicationService.create(createGameCommand);

		assertThat(response).isNotNull();
		assertThat(response.homeTeamId()).isEqualTo(createGameCommand.homeTeamId());
		assertThat(response.awayTeamId()).isEqualTo(createGameCommand.awayTeamId());
		assertThat(response.stadiumId()).isEqualTo(createGameCommand.stadiumId());

		verify(gameScheduleRepository, times(1)).save(any(GameScheduleEntity.class));
		verify(baseballGameStatusRepository, times(1)).save(any(BaseballGameStatusEntity.class));
	}

	@Test
	@DisplayName("method: create() - 동일 경기 일정 존재 시 생성 실패")
	void 경기_생성_실패_중복_일정() {
		given(gameScheduleRepository.existsByHomeTeamIdAndAwayTeamIdAndPlayDateAndStartAt(
			any(UUID.class), any(UUID.class), any(LocalDate.class), any(LocalTime.class)
		)).willReturn(true);

		assertThatThrownBy(() -> baseballGameApplicationService.create(createGameCommand))
			.isInstanceOf(CustomException.class)
			.hasMessageContaining(ErrorCode.GAME_ALREADY_EXISTS.getMessage());

		verify(gameScheduleRepository, never()).save(any(GameScheduleEntity.class));
		verify(baseballGameStatusRepository, never()).save(any(BaseballGameStatusEntity.class));
	}
}
