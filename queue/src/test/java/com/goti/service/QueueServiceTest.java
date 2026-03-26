package com.goti.service;

import com.goti.GotiQueueApplication;
import com.goti.dto.response.QueueStatusResponse;
import com.goti.dto.response.QueueValidateResponse;

import com.goti.infra.cache.RedisCache;

import com.goti.infra.constants.redis.RedisKey;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest(classes = {
	GotiQueueApplication.class,
	QueueService.class
})
public class QueueServiceTest {

	@Autowired
	private QueueService queueService;

	@Autowired
	RedisCache redisCache;

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	private UUID gameId;
	private UUID memberId;

	@BeforeEach
	void setup() {
		gameId = UUID.randomUUID();
		memberId = UUID.randomUUID();
	}

	@Test
	@DisplayName("대기열 진입 시 순번이 부여 및 Polling 시 순번 조회 가능")
	void 대기열_진입_순번_부여_및_polling_순번_조회_대기자_없음() {
		QueueValidateResponse validateResponse = queueService.validate(gameId, memberId);

		assertThat(validateResponse.isPassed())
			.isTrue();

		assertThat(validateResponse.token())
			.isNotNull();
	}

	@Test
	void 대기자가_없고_허용인원_미만_즉시_통과_성공() {
		QueueValidateResponse validateResponse = queueService.validate(gameId, memberId);

		assertThat(validateResponse.isPassed()).isTrue();
		assertThat(validateResponse.token()).isNotNull();
		assertThat(validateResponse.rank()).isEqualTo(0L);
	}

	@Test
	void 대기자가_없을_때_진입_후_상태_조회_시_통과_성공() {
		queueService.validate(gameId, memberId);
		QueueStatusResponse statusResponse = queueService.getStatus(gameId, memberId);

		assertThat(statusResponse.isPassed()).isTrue();
		assertThat(statusResponse.rank()).isEqualTo(0L);
		assertThat(statusResponse.token()).isNotNull();
	}

	@Test
	void 허용인원_초과시_대기_상태_전환_성공() {
		for (int i = 0; i < 100; i++) {
			String passedKey = RedisKey.QUEUE_PASSED.getKey(gameId, UUID.randomUUID());
			redisCache.set(passedKey, "dummy-token", RedisKey.QUEUE_PASSED.getTtl());
		}

		QueueValidateResponse validateResponse = queueService.validate(gameId, memberId);

		assertThat(validateResponse.isPassed()).isFalse();
		assertThat(validateResponse.rank()).isEqualTo(1L);
	}

}
