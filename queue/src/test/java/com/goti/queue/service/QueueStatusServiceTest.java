package com.goti.queue.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.queue.domain.model.QueueMeta;
import com.goti.queue.dto.response.QueueStatusResponse;
import com.goti.queue.repository.QueueRedisRepository;

@ExtendWith(MockitoExtension.class)
class QueueStatusServiceTest {

	@Mock
	private QueueRedisRepository queueRedisRepository;

	@InjectMocks
	private QueueStatusService queueStatusService;

	private UUID gameId;
	private UUID userId;

	@BeforeEach
	void setUp() {
		gameId = UUID.randomUUID();
		userId = UUID.randomUUID();
	}

	@Test
	void 상태_조회시_입장가능_인원_및_공개순번_계산() {
		QueueMeta queueMeta = new QueueMeta(
			5000L,
			1900L,
			0L,
			2000L,
			1900L,
			Instant.parse("2026-03-25T10:15:30Z")
		);
		given(queueRedisRepository.getMeta(gameId)).willReturn(queueMeta);

		QueueStatusResponse response = queueStatusService.getStatus(gameId, userId);

		assertEquals(gameId, response.gameId());
		assertEquals(5000L, response.maxCapacity());
		assertEquals(1900L, response.activeCount());
		assertEquals(3100L, response.availableSlots());
		assertEquals(2000L, response.currentAllowedRank());
		assertEquals(5100L, response.publishedRank());
		assertEquals(queueMeta.updatedAt(), response.updatedAt());
	}

	@Test
	void 현재인원이_최대인원보다_크면_입장가능인원_0으로_보정() {
		QueueMeta queueMeta = new QueueMeta(
			100L,
			150L,
			0L,
			200L,
			150L,
			Instant.parse("2026-03-25T10:15:30Z")
		);
		given(queueRedisRepository.getMeta(gameId)).willReturn(queueMeta);

		QueueStatusResponse response = queueStatusService.getStatus(gameId, userId);

		assertThat(response.availableSlots()).isZero();
		assertEquals(200L, response.publishedRank());
	}

	@Test
	void userId_없으면_인증예외_반환() {
		assertThatThrownBy(() -> queueStatusService.getStatus(gameId, null))
			.isInstanceOf(CustomException.class)
			.extracting("error")
			.isEqualTo(ErrorCode.AUTH_INVALID);
	}

	@Test
	void 메타정보_없으면_예외_반환() {
		given(queueRedisRepository.getMeta(gameId)).willReturn(null);

		assertThatThrownBy(() -> queueStatusService.getStatus(gameId, userId))
			.isInstanceOf(CustomException.class)
			.extracting("error")
			.isEqualTo(ErrorCode.QUEUE_META_NOT_FOUND);
	}
}
