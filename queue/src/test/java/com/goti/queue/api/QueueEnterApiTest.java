package com.goti.queue.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
@DisplayName("대기열 진입 통합 테스트 - POST /api/v1/queue/enter")
class QueueEnterApiTest extends PostgreSqlContainerSupport {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	private UUID lastGameId;
	private UUID lastUserId;
	private UUID lastSecondUserId;

	@AfterEach
	void tearDown() {
		if (lastGameId == null) {
			return;
		}

		redisTemplate.delete(RedisKey.QUEUE_SEQUENCE.getKey(lastGameId));
		redisTemplate.delete(RedisKey.QUEUE_WAITING.getKey(lastGameId));
		redisTemplate.delete(RedisKey.QUEUE_META.getKey(lastGameId));

		if (lastUserId != null) {
			redisTemplate.delete(RedisKey.QUEUE_ENTRY.getKey(lastGameId, lastUserId));
		}

		if (lastSecondUserId != null) {
			redisTemplate.delete(RedisKey.QUEUE_ENTRY.getKey(lastGameId, lastSecondUserId));
		}
	}

	@Test
	void 첫_진입시_순번과_레디스_저장값_생성() throws Exception {
		lastGameId = UUID.randomUUID();
		lastUserId = UUID.randomUUID();

		MvcResult result = mockMvc.perform(
				post("/api/v1/queue/enter")
					.with(authentication(auth(lastUserId)))
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"gameId":"%s"}
						""".formatted(lastGameId))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.queueToken").isString())
			.andExpect(jsonPath("$.data.queueNumber").value(1))
			.andExpect(jsonPath("$.data.gameId").value(lastGameId.toString()))
			.andReturn();

		JsonNode data = readData(result);
		String waitingKey = RedisKey.QUEUE_WAITING.getKey(lastGameId);
		String entryKey = RedisKey.QUEUE_ENTRY.getKey(lastGameId, lastUserId);

		Double score = redisTemplate.opsForZSet().score(waitingKey, lastUserId.toString());
		Object saved = redisTemplate.opsForValue().get(entryKey);

		assertThat(score).isEqualTo(1.0);
		QueueEntry entry = objectMapper.convertValue(saved, QueueEntry.class);
		assertThat(entry.queueNumber()).isEqualTo(1L);
		assertThat(entry.status()).isEqualTo(QueueStatus.WAITING);
		assertThat(entry.issuedAt()).isNotNull();
		assertThat(data.get("issuedAt").asText()).isEqualTo(entry.issuedAt().toString());
	}

	@Test
	void 두번째_유저_다음_순번_발급() throws Exception {
		lastGameId = UUID.randomUUID();
		lastUserId = UUID.randomUUID();
		lastSecondUserId = UUID.randomUUID();

		mockMvc.perform(
				post("/api/v1/queue/enter")
					.with(authentication(auth(lastUserId)))
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"gameId":"%s"}
						""".formatted(lastGameId))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.queueNumber").value(1));

		mockMvc.perform(
				post("/api/v1/queue/enter")
					.with(authentication(auth(lastSecondUserId)))
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"gameId":"%s"}
						""".formatted(lastGameId))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.queueNumber").value(2));

		assertThat(redisTemplate.opsForZSet().score(RedisKey.QUEUE_WAITING.getKey(lastGameId), lastUserId.toString()))
			.isEqualTo(1.0);
		assertThat(redisTemplate.opsForZSet().score(RedisKey.QUEUE_WAITING.getKey(lastGameId), lastSecondUserId.toString()))
			.isEqualTo(2.0);
	}

	@Test
	void 같은_유저_재진입시_기존_엔트리_삭제_및_새_순번_발급() throws Exception {
		lastGameId = UUID.randomUUID();
		lastUserId = UUID.randomUUID();

		MvcResult firstResult = mockMvc.perform(
				post("/api/v1/queue/enter")
					.with(authentication(auth(lastUserId)))
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"gameId":"%s"}
						""".formatted(lastGameId))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.queueNumber").value(1))
			.andReturn();

		MvcResult secondResult = mockMvc.perform(
				post("/api/v1/queue/enter")
					.with(authentication(auth(lastUserId)))
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"gameId":"%s"}
						""".formatted(lastGameId))
			)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.data.queueNumber").value(2))
			.andReturn();

		JsonNode firstData = readData(firstResult);
		JsonNode secondData = readData(secondResult);

		Object saved = redisTemplate.opsForValue().get(RedisKey.QUEUE_ENTRY.getKey(lastGameId, lastUserId));
		Double score = redisTemplate.opsForZSet().score(RedisKey.QUEUE_WAITING.getKey(lastGameId), lastUserId.toString());
		Long size = redisTemplate.opsForZSet().zCard(RedisKey.QUEUE_WAITING.getKey(lastGameId));

		assertThat(firstData.get("queueToken").asText()).isNotEqualTo(secondData.get("queueToken").asText());
		QueueEntry entry = objectMapper.convertValue(saved, QueueEntry.class);
		assertThat(entry.queueNumber()).isEqualTo(2L);
		assertThat(score).isEqualTo(2.0);
		assertThat(size).isEqualTo(1L);
	}

	@Test
	@SuppressWarnings("unchecked")
	void 메타_없으면_기본값으로_초기화() throws Exception {
		lastGameId = UUID.randomUUID();
		lastUserId = UUID.randomUUID();

		mockMvc.perform(
				post("/api/v1/queue/enter")
					.with(authentication(auth(lastUserId)))
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
						{"gameId":"%s"}
						""".formatted(lastGameId))
			)
			.andExpect(status().isOk());

		Map<Object, Object> meta = redisTemplate.opsForHash().entries(RedisKey.QUEUE_META.getKey(lastGameId));

		assertThat(((Number)meta.get(QueueMetaField.MAX_CAPACITY)).longValue()).isEqualTo(5000L);
		assertThat(((Number)meta.get(QueueMetaField.ACTIVE_COUNT)).longValue()).isEqualTo(0L);
		assertThat(((Number)meta.get(QueueMetaField.PUBLISHED_RANK)).longValue()).isEqualTo(0L);
		assertThat(((Number)meta.get(QueueMetaField.CURRENT_ALLOWED_RANK)).longValue()).isEqualTo(0L);
		assertThat(((Number)meta.get(QueueMetaField.LAST_ENTERED_RANK)).longValue()).isEqualTo(0L);
		assertThat(meta.get(QueueMetaField.UPDATED_AT)).isNotNull();
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
