package com.goti.queue.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.constants.UserRole;
import com.goti.constants.messages.ErrorCode;
import com.goti.infra.constants.redis.RedisKey;
import com.goti.queue.GotiQueueApplication;
import com.goti.queue.constants.QueueMetaField;
import com.goti.queue.constants.QueueStatus;
import com.goti.queue.domain.model.QueueEntry;
import com.goti.queue.support.PostgreSqlContainerSupport;
import com.goti.security.SimpleUserDetails;

@SpringBootTest(classes = GotiQueueApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("대기열 최종 입장 통합 테스트 - POST /api/v1/queue/{gameId}/seat-enter")
class QueueSeatEnterApiTest extends PostgreSqlContainerSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private UUID gameId;
	private UUID userId;

	@AfterEach
	void tearDown() {
		if (gameId == null) {
			return;
		}
		redisTemplate.delete(RedisKey.QUEUE_SEQUENCE.getKey(gameId));
		redisTemplate.delete(RedisKey.QUEUE_WAITING.getKey(gameId));
		redisTemplate.delete(RedisKey.QUEUE_META.getKey(gameId));
		redisTemplate.delete(RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId));
		if (userId != null) {
			redisTemplate.delete(RedisKey.QUEUE_ENTRY.getKey(gameId, userId));
		}
	}

	@Test
	void seat_enter_성공시_admitted_처리와_활성유저_등록() throws Exception {
		gameId = UUID.randomUUID();
		userId = UUID.randomUUID();

		MvcResult enterResult = mockMvc.perform(
				post("/api/v1/queue/enter")
					.with(authentication(auth(userId)))
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"gameId":"%s"}
						""".formatted(gameId))
			)
			.andExpect(status().isOk())
			.andReturn();

		String queueToken = readData(enterResult).get("queueToken").asText();

		redisTemplate.opsForHash().putAll(RedisKey.QUEUE_META.getKey(gameId), Map.of(
			QueueMetaField.MAX_CAPACITY, 5000L,
			QueueMetaField.ACTIVE_COUNT, 0L,
			QueueMetaField.PUBLISHED_RANK, 10L,
			QueueMetaField.CURRENT_ALLOWED_RANK, 10L,
			QueueMetaField.LAST_ENTERED_RANK, 0L,
			QueueMetaField.UPDATED_AT, Instant.parse("2026-03-25T10:15:30Z").toString()
		));

		mockMvc.perform(
				post("/api/v1/queue/{gameId}/seat-enter", gameId)
					.with(authentication(auth(userId)))
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"queueToken":"%s"}
						""".formatted(queueToken))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.gameId").value(gameId.toString()))
			.andExpect(jsonPath("$.data.enterAllowed").value(true))
			.andExpect(jsonPath("$.data.queueNumber").value(1))
			.andExpect(jsonPath("$.data.status").value("ADMITTED"));

		Object saved = redisTemplate.opsForValue().get(RedisKey.QUEUE_ENTRY.getKey(gameId, userId));
		QueueEntry entry = objectMapper.convertValue(saved, QueueEntry.class);

		assertThat(entry.status()).isEqualTo(QueueStatus.ADMITTED);
		assertThat(redisTemplate.opsForSet().isMember(RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId), userId.toString()))
			.isTrue();
		assertThat(redisTemplate.opsForZSet().score(RedisKey.QUEUE_WAITING.getKey(gameId), userId.toString()))
			.isNull();
		assertThat(((Number)redisTemplate.opsForHash()
			.get(RedisKey.QUEUE_META.getKey(gameId), QueueMetaField.ACTIVE_COUNT)).longValue()).isEqualTo(1L);
	}

	@Test
	void 수용인원_초과시_seat_enter_실패() throws Exception {
		gameId = UUID.randomUUID();
		userId = UUID.randomUUID();

		MvcResult enterResult = mockMvc.perform(
				post("/api/v1/queue/enter")
					.with(authentication(auth(userId)))
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"gameId":"%s"}
						""".formatted(gameId))
			)
			.andExpect(status().isOk())
			.andReturn();

		String queueToken = readData(enterResult).get("queueToken").asText();

		// 수용인원 가득 참 → publishedRank=max(5000, 5000+0)=5000, 하지만 capacity 먼저 체크
		redisTemplate.opsForHash().putAll(RedisKey.QUEUE_META.getKey(gameId), Map.of(
			QueueMetaField.MAX_CAPACITY, 5000L,
			QueueMetaField.ACTIVE_COUNT, 5000L,
			QueueMetaField.PUBLISHED_RANK, 5000L,
			QueueMetaField.CURRENT_ALLOWED_RANK, 5000L,
			QueueMetaField.LAST_ENTERED_RANK, 5000L,
			QueueMetaField.UPDATED_AT, Instant.parse("2026-03-25T10:15:30Z").toString()
		));

		mockMvc.perform(
				post("/api/v1/queue/{gameId}/seat-enter", gameId)
					.with(authentication(auth(userId)))
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"queueToken":"%s"}
						""".formatted(queueToken))
			)
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message").value(ErrorCode.QUEUE_CAPACITY_FULL.getMessage()));
	}

	private Authentication auth(UUID userId) {
		SimpleUserDetails principal = new SimpleUserDetails(userId, UserRole.MEMBER.name());
		return UsernamePasswordAuthenticationToken.authenticated(
			principal,
			null,
			principal.getAuthorities()
		);
	}

	private JsonNode readData(MvcResult result) throws Exception {
		return objectMapper.readTree(result.getResponse().getContentAsString()).get("data");
	}
}
