package com.goti.queue.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.goti.constants.UserRole;
import com.goti.constants.messages.ErrorCode;
import com.goti.infra.constants.redis.RedisKey;
import com.goti.queue.GotiQueueApplication;
import com.goti.queue.constants.QueueMetaField;
import com.goti.queue.support.PostgreSqlContainerSupport;
import com.goti.security.SimpleUserDetails;

@SpringBootTest(classes = GotiQueueApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("대기열 상태 조회 통합 테스트 - GET /api/v1/queue/{gameId}/status")
class QueueStatusApiTest extends PostgreSqlContainerSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private UUID gameId;

	@AfterEach
	void tearDown() {
		if (gameId != null) {
			redisTemplate.delete(RedisKey.QUEUE_META.getKey(gameId));
		}
	}

	@Test
	void 대기열_상태_조회_성공() throws Exception {
		gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		redisTemplate.opsForHash().putAll(RedisKey.QUEUE_META.getKey(gameId), Map.of(
			QueueMetaField.MAX_CAPACITY, 5000L,
			QueueMetaField.ACTIVE_COUNT, 1900L,
			QueueMetaField.PUBLISHED_RANK, 0L,
			QueueMetaField.CURRENT_ALLOWED_RANK, 2000L,
			QueueMetaField.LAST_ENTERED_RANK, 1900L,
			QueueMetaField.UPDATED_AT, Instant.parse("2026-03-25T10:15:30Z").toString()
		));

		mockMvc.perform(
				get("/api/v1/queue/{gameId}/status", gameId)
					.with(authentication(auth(userId)))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.gameId").value(gameId.toString()))
			.andExpect(jsonPath("$.data.maxCapacity").value(5000))
			.andExpect(jsonPath("$.data.activeCount").value(1900))
			.andExpect(jsonPath("$.data.availableSlots").value(3100))
			.andExpect(jsonPath("$.data.currentAllowedRank").value(5000))
			.andExpect(jsonPath("$.data.publishedRank").value(5000));
	}

	@Test
	void 메타정보_없으면_예외_반환() throws Exception {
		gameId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();

		mockMvc.perform(
				get("/api/v1/queue/{gameId}/status", gameId)
					.with(authentication(auth(userId)))
			)
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value(ErrorCode.QUEUE_META_NOT_FOUND.getMessage()));
	}

	private Authentication auth(UUID userId) {
		SimpleUserDetails principal = new SimpleUserDetails(userId, UserRole.MEMBER.name());
		return UsernamePasswordAuthenticationToken.authenticated(
			principal,
			null,
			principal.getAuthorities()
		);
	}
}
