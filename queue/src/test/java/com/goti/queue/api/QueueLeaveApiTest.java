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
import com.goti.queue.GotiQueueApplication;
import com.goti.queue.constants.QueueMetaField;
import com.goti.queue.constants.QueueRedisKey;
import com.goti.queue.constants.QueueStatus;
import com.goti.queue.domain.model.QueueEntry;
import com.goti.security.SimpleUserDetails;

@SpringBootTest(classes = GotiQueueApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("대기열 이탈 통합 테스트 - POST /api/v1/queue/{gameId}/leave")
class QueueLeaveApiTest {

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
		redisTemplate.delete(QueueRedisKey.SEQUENCE.getKey(gameId));
		redisTemplate.delete(QueueRedisKey.WAITING.getKey(gameId));
		redisTemplate.delete(QueueRedisKey.META.getKey(gameId));
		redisTemplate.delete(QueueRedisKey.ACTIVE_USERS.getKey(gameId));
		if (userId != null) {
			redisTemplate.delete(QueueRedisKey.ENTRY.getKey(gameId, userId));
		}
	}

	@Test
	void leave_호출시_activeCount_감소와_이탈_처리() throws Exception {
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

		redisTemplate.opsForHash().putAll(QueueRedisKey.META.getKey(gameId), Map.of(
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
			.andExpect(status().isOk());

		mockMvc.perform(
				post("/api/v1/queue/{gameId}/leave", gameId)
					.with(authentication(auth(userId)))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.gameId").value(gameId.toString()))
			.andExpect(jsonPath("$.data.released").value(true))
			.andExpect(jsonPath("$.data.status").value("LEFT"));

		Object saved = redisTemplate.opsForValue().get(QueueRedisKey.ENTRY.getKey(gameId, userId));
		QueueEntry entry = objectMapper.convertValue(saved, QueueEntry.class);

		assertThat(entry.status()).isEqualTo(QueueStatus.LEFT);
		assertThat(redisTemplate.opsForSet().isMember(QueueRedisKey.ACTIVE_USERS.getKey(gameId), userId.toString()))
			.isFalse();
		assertThat(((Number)redisTemplate.opsForHash()
			.get(QueueRedisKey.META.getKey(gameId), QueueMetaField.ACTIVE_COUNT)).longValue()).isZero();
	}

	@Test
	void leave_중복_호출시_두번째는_noop() throws Exception {
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

		redisTemplate.opsForHash().putAll(QueueRedisKey.META.getKey(gameId), Map.of(
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
			.andExpect(status().isOk());

		mockMvc.perform(
				post("/api/v1/queue/{gameId}/leave", gameId)
					.with(authentication(auth(userId)))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.released").value(true));

		mockMvc.perform(
				post("/api/v1/queue/{gameId}/leave", gameId)
					.with(authentication(auth(userId)))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.released").value(false))
			.andExpect(jsonPath("$.data.status").value("LEFT"));

		assertThat(((Number)redisTemplate.opsForHash()
			.get(QueueRedisKey.META.getKey(gameId), QueueMetaField.ACTIVE_COUNT)).longValue()).isZero();
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
