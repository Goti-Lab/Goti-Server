package com.goti.queue.repository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.infra.constants.redis.RedisKey;
import com.goti.queue.constants.QueueMetaField;
import com.goti.queue.domain.model.QueueEntry;
import com.goti.queue.domain.model.QueueMeta;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class QueueRedisRepository {

	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	// Lua script ARGV 직렬화용 — GenericJackson2JsonRedisSerializer는 ARGV를
	// JSON 직렬화하여 Hash 필드명과 불일치. StringRedisTemplate은 plain string 전달.
	private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

	public QueueEntry getEntry(UUID gameId, UUID userId) {
		Object value = redisTemplate.opsForValue().get(RedisKey.QUEUE_ENTRY.getKey(gameId, userId));
		if (value == null) {
			return null;
		}
		if (value instanceof QueueEntry entry) {
			return entry;
		}
		return objectMapper.convertValue(value, QueueEntry.class);
	}

	public void saveEntry(UUID gameId, UUID userId, QueueEntry entry, Duration ttl) {
		redisTemplate.opsForValue().set(
			RedisKey.QUEUE_ENTRY.getKey(gameId, userId),
			entry,
			ttl
		);
	}

	public void deleteEntry(UUID gameId, UUID userId) {
		redisTemplate.delete(RedisKey.QUEUE_ENTRY.getKey(gameId, userId));
	}

	public long nextSequence(UUID gameId) {
		Long sequence = redisTemplate.opsForValue().increment(RedisKey.QUEUE_SEQUENCE.getKey(gameId));
		return sequence == null ? 1L : sequence;
	}

	public void addWaiting(UUID gameId, UUID userId, long queueNumber) {
		redisTemplate.opsForZSet().add(
			RedisKey.QUEUE_WAITING.getKey(gameId),
			userId.toString(),
			queueNumber
		);
	}

	public void removeWaiting(UUID gameId, UUID userId) {
		redisTemplate.opsForZSet().remove(
			RedisKey.QUEUE_WAITING.getKey(gameId),
			userId.toString()
		);
	}

	public long countWaitingUsers(UUID gameId) {
		Long count = redisTemplate.opsForZSet().zCard(RedisKey.QUEUE_WAITING.getKey(gameId));
		return count == null ? 0L : count;
	}

	public boolean isActiveUser(UUID gameId, UUID userId) {
		Boolean member = redisTemplate.opsForSet().isMember(
			RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId),
			userId.toString()
		);
		return Boolean.TRUE.equals(member);
	}

	public void addActiveUser(UUID gameId, UUID userId) {
		redisTemplate.opsForSet().add(
			RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId),
			userId.toString()
		);
	}

	public void removeActiveUser(UUID gameId, UUID userId) {
		redisTemplate.opsForSet().remove(
			RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId),
			userId.toString()
		);
	}

	public void addExpirationUser(UUID gameId, UUID userId, Instant expiresAt) {
		redisTemplate.opsForZSet().add(
			RedisKey.QUEUE_EXPIRATION_USERS.getKey(),
			expirationMember(gameId, userId),
			expiresAt.toEpochMilli()
		);
	}

	public void removeExpirationUser(UUID gameId, UUID userId) {
		redisTemplate.opsForZSet().remove(
			RedisKey.QUEUE_EXPIRATION_USERS.getKey(),
			expirationMember(gameId, userId)
		);
	}

	public Set<Object> getExpiredUsers(Instant now) {
		return redisTemplate.opsForZSet().rangeByScore(
			RedisKey.QUEUE_EXPIRATION_USERS.getKey(),
			0,
			now.toEpochMilli()
		);
	}

	public void initializeMetaIfAbsent(UUID gameId, long maxCapacity) {
		String metaKey = RedisKey.QUEUE_META.getKey(gameId);
		if (stringRedisTemplate.hasKey(metaKey)) {
			return;
		}

		// TODO: 구조가 잡힌 뒤 queue open/init 단계에서만 메타를 생성하도록 변경
		stringRedisTemplate.opsForHash().putAll(metaKey, Map.of(
			QueueMetaField.MAX_CAPACITY, String.valueOf(maxCapacity),
			QueueMetaField.ACTIVE_COUNT, "0",
			QueueMetaField.PUBLISHED_RANK, "0",
			QueueMetaField.CURRENT_ALLOWED_RANK, "0",
			QueueMetaField.LAST_ENTERED_RANK, "0",
			QueueMetaField.UPDATED_AT, Instant.now().toString()
		));
	}

	public QueueMeta getMeta(UUID gameId) {
		Map<Object, Object> meta = stringRedisTemplate.opsForHash().entries(RedisKey.QUEUE_META.getKey(gameId));
		if (meta.isEmpty()) {
			return null;
		}

		return new QueueMeta(
			longFromString(meta.get(QueueMetaField.MAX_CAPACITY)),
			longFromString(meta.get(QueueMetaField.ACTIVE_COUNT)),
			longFromString(meta.get(QueueMetaField.PUBLISHED_RANK)),
			longFromString(meta.get(QueueMetaField.CURRENT_ALLOWED_RANK)),
			longFromString(meta.get(QueueMetaField.LAST_ENTERED_RANK)),
			Instant.parse(String.valueOf(meta.get(QueueMetaField.UPDATED_AT)))
		);
	}

	/**
	 * activeCount < maxCapacity일 때만 원자적으로 increment.
	 * Lua Script로 check-and-increment를 단일 Redis 명령으로 실행.
	 * @return true: 승격 성공, false: 수용량 초과 (increment 안 함)
	 */
	private static final String TRY_INCREMENT_ACTIVE_SCRIPT_STR =
		"local key = KEYS[1] " +
		"local active = tonumber(redis.call('HGET', key, ARGV[1]) or '0') " +
		"local max = tonumber(redis.call('HGET', key, ARGV[2]) or '0') " +
		"if active < max then " +
		"  redis.call('HINCRBY', key, ARGV[1], 1) " +
		"  redis.call('HSET', key, ARGV[3], ARGV[4]) " +
		"  return 1 " +
		"else " +
		"  return 0 " +
		"end";
	private static final RedisScript<Long> TRY_INCREMENT_ACTIVE_SCRIPT =
		RedisScript.of(TRY_INCREMENT_ACTIVE_SCRIPT_STR, Long.class);

	public boolean tryIncrementActiveCount(UUID gameId) {
		Long result = stringRedisTemplate.execute(
			TRY_INCREMENT_ACTIVE_SCRIPT,
			List.of(RedisKey.QUEUE_META.getKey(gameId)),
			QueueMetaField.ACTIVE_COUNT,
			QueueMetaField.MAX_CAPACITY,
			QueueMetaField.UPDATED_AT,
			Instant.now().toString()
		);
		return result != null && result == 1L;
	}

	public long incrementActiveCount(UUID gameId) {
		Long result = stringRedisTemplate.opsForHash().increment(
			RedisKey.QUEUE_META.getKey(gameId), QueueMetaField.ACTIVE_COUNT, 1L
		);
		stringRedisTemplate.opsForHash().put(
			RedisKey.QUEUE_META.getKey(gameId), QueueMetaField.UPDATED_AT, Instant.now().toString()
		);
		return result == null ? 1L : result;
	}

	public long decrementActiveCount(UUID gameId) {
		Long result = stringRedisTemplate.opsForHash().increment(
			RedisKey.QUEUE_META.getKey(gameId), QueueMetaField.ACTIVE_COUNT, -1L
		);
		stringRedisTemplate.opsForHash().put(
			RedisKey.QUEUE_META.getKey(gameId), QueueMetaField.UPDATED_AT, Instant.now().toString()
		);
		return result == null ? 0L : Math.max(0L, result);
	}

	/**
	 * lastEnteredRank / currentAllowedRank를 원자적으로 갱신.
	 * 동시 seat-enter 시 race condition 방지 — max(기존값, 새값)으로만 갱신.
	 * WHY: putAll은 last-write-wins라서 늦게 도착한 낮은 queueNumber가 높은 값을 덮어쓸 수 있음.
	 */
	private static final String UPDATE_SEAT_ENTER_META_SCRIPT_STR =
		"local key = KEYS[1] " +
		"local newRank = tonumber(ARGV[1]) " +
		"local curLast = tonumber(redis.call('HGET', key, ARGV[2]) or '0') " +
		"local curAllowed = tonumber(redis.call('HGET', key, ARGV[3]) or '0') " +
		"if newRank > curLast then " +
		"  redis.call('HSET', key, ARGV[2], newRank) " +
		"end " +
		"if newRank > curAllowed then " +
		"  redis.call('HSET', key, ARGV[3], newRank) " +
		"end " +
		"redis.call('HSET', key, ARGV[4], ARGV[5]) " +
		"return 1";
	private static final RedisScript<Long> UPDATE_SEAT_ENTER_META_SCRIPT =
		RedisScript.of(UPDATE_SEAT_ENTER_META_SCRIPT_STR, Long.class);

	public void updateSeatEnterMeta(UUID gameId, long lastEnteredRank) {
		stringRedisTemplate.execute(
			UPDATE_SEAT_ENTER_META_SCRIPT,
			List.of(RedisKey.QUEUE_META.getKey(gameId)),
			String.valueOf(lastEnteredRank),
			QueueMetaField.LAST_ENTERED_RANK,
			QueueMetaField.CURRENT_ALLOWED_RANK,
			QueueMetaField.UPDATED_AT,
			Instant.now().toString()
		);
	}

	public void updateStatusMeta(UUID gameId, long currentAllowedRank, long publishedRank, Instant updatedAt) {
		stringRedisTemplate.opsForHash().putAll(RedisKey.QUEUE_META.getKey(gameId), Map.of(
			QueueMetaField.CURRENT_ALLOWED_RANK, String.valueOf(currentAllowedRank),
			QueueMetaField.PUBLISHED_RANK, String.valueOf(publishedRank),
			QueueMetaField.UPDATED_AT, updatedAt.toString()
		));
	}

	// ── Lua All-in-One: Enter (5-7 RTT → 1 RTT) ─────────────────────
	// 기존 entry 존재 시 cleanup + meta 초기화 + sequence 발급 + waiting 추가를 1 Lua로 통합.
	// saveEntry(JSON 직렬화)만 별도 호출 필요 → 전체 2 RTT.
	// KEYS: [1]=META, [2]=SEQUENCE, [3]=WAITING, [4]=ENTRY, [5]=ACTIVE_USERS, [6]=EXPIRATION_USERS
	// ARGV: [1]=userId, [2]=maxCapacity, [3]=now, [4]=expirationMember(gameId:userId)
	// NOTE: standalone Redis 전제. KEYS[6](글로벌 키)은 Cluster 환경에서 cross-slot 에러 발생 → 분리 실행 필요.
	private static final String ENTER_QUEUE_SCRIPT =
		"local existing = redis.call('GET', KEYS[4]) " +
		"local oldQueueNumber = -1 " +
		"if existing then " +
		"  local ok, entry = pcall(cjson.decode, existing) " +
		"  if ok and entry.queueNumber then oldQueueNumber = entry.queueNumber end " +
		"  redis.call('ZREM', KEYS[3], ARGV[1]) " +
		"  redis.call('DEL', KEYS[4]) " +
		// re-enter 시 ADMITTED 상태의 잔여 active-users + expiration 정리
		// WHY: leave 없이 브라우저를 닫고 재진입하면 active-users에 좀비가 남아 409 유발
		"  if redis.call('SISMEMBER', KEYS[5], ARGV[1]) == 1 then " +
		"    redis.call('SREM', KEYS[5], ARGV[1]) " +
		"    if redis.call('EXISTS', KEYS[1]) == 1 then " +
		"      local cur = tonumber(redis.call('HGET', KEYS[1], 'activeCount') or '0') " +
		"      if cur > 0 then redis.call('HINCRBY', KEYS[1], 'activeCount', -1) end " +
		"    end " +
		"  end " +
		"  redis.call('ZREM', KEYS[6], ARGV[4]) " +
		"end " +
		"if redis.call('EXISTS', KEYS[1]) == 0 then " +
		"  redis.call('HSET', KEYS[1], " +
		"    'maxCapacity', ARGV[2], 'activeCount', '0', " +
		"    'publishedRank', '0', 'currentAllowedRank', '0', " +
		"    'lastEnteredRank', '0', 'updatedAt', ARGV[3]) " +
		"end " +
		"local seq = redis.call('INCR', KEYS[2]) " +
		"redis.call('ZADD', KEYS[3], seq, ARGV[1]) " +
		"return {seq, oldQueueNumber}";

	/**
	 * Enter All-in-One Lua.
	 * @return {newQueueNumber, oldQueueNumber}. oldQueueNumber == -1이면 신규 진입.
	 */
	public List<Long> enterQueue(UUID gameId, UUID userId, long maxCapacity) {
		List<String> keys = List.of(
			RedisKey.QUEUE_META.getKey(gameId),          // KEYS[1]
			RedisKey.QUEUE_SEQUENCE.getKey(gameId),      // KEYS[2]
			RedisKey.QUEUE_WAITING.getKey(gameId),       // KEYS[3]
			RedisKey.QUEUE_ENTRY.getKey(gameId, userId), // KEYS[4]
			RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId),  // KEYS[5]
			RedisKey.QUEUE_EXPIRATION_USERS.getKey()     // KEYS[6]
		);
		@SuppressWarnings("unchecked")
		List<Long> result = stringRedisTemplate.execute(
			RedisScript.of(ENTER_QUEUE_SCRIPT, List.class),
			keys,
			userId.toString(),                           // ARGV[1]
			String.valueOf(maxCapacity),                  // ARGV[2]
			Instant.now().toString(),                     // ARGV[3]
			expirationMember(gameId, userId)              // ARGV[4]
		);
		return result;
	}

	// ── Lua All-in-One: Seat-Enter / Admit (9 RTT → 1 RTT) ──────────
	// entry 검증 + active 체크 + publishedRank 계산 + capacity 체크 + increment
	// + rank 갱신 + active 추가 + waiting 제거 + expiration 추가를 1 Lua로 통합.
	private static final String TRY_ADMIT_SCRIPT =
		"local raw = redis.call('GET', KEYS[1]) " +
		"if not raw then return {-1} end " +
		"local ok, entry = pcall(cjson.decode, raw) " +
		"if not ok then return {-1} end " +
		"if entry.status ~= 'WAITING' then return {-2} end " +
		"if tostring(entry.queueNumber) ~= ARGV[2] then return {-3} end " +

		"if redis.call('SISMEMBER', KEYS[2], ARGV[1]) == 1 then return {-4} end " +

		"local active = tonumber(redis.call('HGET', KEYS[3], 'activeCount') or '0') " +
		"local max = tonumber(redis.call('HGET', KEYS[3], 'maxCapacity') or '0') " +
		"local curAllowed = tonumber(redis.call('HGET', KEYS[3], 'currentAllowedRank') or '0') " +
		"local lastEntered = tonumber(redis.call('HGET', KEYS[3], 'lastEnteredRank') or '0') " +
		"local available = math.max(0, max - active) " +
		"local publishedRank = math.max(curAllowed, lastEntered + available) " +

		"local qn = tonumber(entry.queueNumber) " +
		"if qn > publishedRank then return {-5} end " +

		"if active >= max then return {-6} end " +
		"redis.call('HINCRBY', KEYS[3], 'activeCount', 1) " +

		"if qn > lastEntered then redis.call('HSET', KEYS[3], 'lastEnteredRank', qn) end " +
		"if qn > curAllowed then redis.call('HSET', KEYS[3], 'currentAllowedRank', qn) end " +
		"redis.call('HSET', KEYS[3], 'updatedAt', ARGV[3]) " +

		"redis.call('SADD', KEYS[2], ARGV[1]) " +
		"redis.call('ZREM', KEYS[4], ARGV[1]) " +
		"redis.call('ZADD', KEYS[5], tonumber(ARGV[4]), ARGV[5]) " +

		"return {1, qn}";

	/**
	 * Seat-Enter All-in-One Lua.
	 * @return 코드 목록. [0] = 결과코드: 1=성공, -1=ENTRY_NOT_FOUND, -2/-3=ENTRY_MISMATCH,
	 *         -4=ALREADY_ADMITTED, -5=NOT_ALLOWED_YET, -6=CAPACITY_FULL.
	 *         성공 시 [1] = queueNumber.
	 */
	public List<Long> tryAdmit(UUID gameId, UUID userId, long expectedQueueNumber, Instant expiresAt) {
		List<String> keys = List.of(
			RedisKey.QUEUE_ENTRY.getKey(gameId, userId),
			RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId),
			RedisKey.QUEUE_META.getKey(gameId),
			RedisKey.QUEUE_WAITING.getKey(gameId),
			RedisKey.QUEUE_EXPIRATION_USERS.getKey()
		);
		@SuppressWarnings("unchecked")
		List<Long> result = stringRedisTemplate.execute(
			RedisScript.of(TRY_ADMIT_SCRIPT, List.class),
			keys,
			userId.toString(),
			String.valueOf(expectedQueueNumber),
			Instant.now().toString(),
			String.valueOf(expiresAt.toEpochMilli()),
			expirationMember(gameId, userId)
		);
		return result;
	}

	// ── Lua All-in-One: Leave (8 RTT → 1 RTT) ───────────────────────
	// entry 읽기 + active 체크 + activeCount 감소 + active 제거 + expiration 제거를 1 Lua로 통합.
	private static final String EXECUTE_LEAVE_SCRIPT =
		"local raw = redis.call('GET', KEYS[1]) " +
		"local isActive = redis.call('SISMEMBER', KEYS[2], ARGV[1]) " +

		"local status = 'UNKNOWN' " +
		"if raw then " +
		"  local ok, entry = pcall(cjson.decode, raw) " +
		"  if ok then status = entry.status or 'UNKNOWN' end " +
		"end " +

		"if isActive == 0 and (not raw or status == 'LEFT' or status == 'EXPIRED') then " +
		"  return {0, 0} " +
		"end " +

		"if isActive == 1 then " +
		"  redis.call('SREM', KEYS[2], ARGV[1]) " +
		"  if redis.call('EXISTS', KEYS[3]) == 1 then " +
		"    local cur = tonumber(redis.call('HGET', KEYS[3], 'activeCount') or '0') " +
		"    if cur > 0 then redis.call('HINCRBY', KEYS[3], 'activeCount', -1) end " +
		"    redis.call('HSET', KEYS[3], 'updatedAt', ARGV[2]) " +
		"  end " +
		"end " +

		"redis.call('ZREM', KEYS[4], ARGV[3]) " +

		"return {isActive, 1}";

	/**
	 * Leave All-in-One Lua.
	 * @return [0] = released (1=active user가 퇴장, 0=이미 퇴장 또는 비활성),
	 *         [1] = processed (1=처리됨, 0=early return)
	 */
	public List<Long> executeLeave(UUID gameId, UUID userId) {
		List<String> keys = List.of(
			RedisKey.QUEUE_ENTRY.getKey(gameId, userId),
			RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId),
			RedisKey.QUEUE_META.getKey(gameId),
			RedisKey.QUEUE_EXPIRATION_USERS.getKey()
		);
		@SuppressWarnings("unchecked")
		List<Long> result = stringRedisTemplate.execute(
			RedisScript.of(EXECUTE_LEAVE_SCRIPT, List.class),
			keys,
			userId.toString(),
			Instant.now().toString(),
			expirationMember(gameId, userId)
		);
		return result;
	}

	// ── Lua: Game Cleanup (game 단위 전체 키 삭제) ──────────────────
	// WARNING: 진행 중인 game에서 호출하면 활성 사용자 전원이 강제 퇴장됨.
	// 반드시 game 종료 후 또는 dev 테스트 정리 용도로만 사용할 것.
	// NOTE: standalone Redis 전제. Cluster 전환 시 글로벌 키(KEYS[5]) 분리 실행 필요.
	// KEYS: [1]=SEQUENCE, [2]=META, [3]=WAITING, [4]=ACTIVE_USERS, [5]=EXPIRATION_USERS
	// ARGV: [1]=gameId prefix (gameId:)
	private static final String CLEANUP_GAME_SCRIPT =
		// SMEMBERS로 해당 game의 active users만 가져와서 expiration에서 제거 (ZSCAN 전체 순회 방지)
		// NOTE: SMEMBERS는 O(N)이므로 active users가 수만 건이면 blocking 발생.
		// game 종료 시점에는 대부분 leave/expire 처리 후이므로 실제 대상은 수십~수백 수준.
		// prod 스케일(수만) 시 application 레벨 SSCAN batch + pipeline ZREM으로 전환 필요.
		"local users = redis.call('SMEMBERS', KEYS[4]) " +
		"for _, u in ipairs(users) do " +
		"  redis.call('ZREM', KEYS[5], ARGV[1] .. u) " +
		"end " +
		// EXPIRATION_USERS(KEYS[5])는 글로벌 공유 키 → DEL 아닌 개별 ZREM으로 해당 game 엔트리만 제거
		"redis.call('DEL', KEYS[1], KEYS[2], KEYS[3], KEYS[4]) " +
		"return 1";

	/**
	 * 해당 gameId의 모든 queue 관련 Redis 키를 일괄 삭제.
	 * WARNING: 진행 중인 game에서 호출하면 활성 사용자 전원이 강제 퇴장됨.
	 */
	public void cleanupGame(UUID gameId) {
		List<String> keys = List.of(
			RedisKey.QUEUE_SEQUENCE.getKey(gameId),     // KEYS[1]
			RedisKey.QUEUE_META.getKey(gameId),          // KEYS[2]
			RedisKey.QUEUE_WAITING.getKey(gameId),       // KEYS[3]
			RedisKey.QUEUE_ACTIVE_USERS.getKey(gameId),  // KEYS[4]
			RedisKey.QUEUE_EXPIRATION_USERS.getKey()     // KEYS[5]
		);
		stringRedisTemplate.execute(
			RedisScript.of(CLEANUP_GAME_SCRIPT, Long.class),
			keys,
			gameId + ":"                                  // ARGV[1]: gameId prefix for ZSCAN
		);
	}

	private long longFromString(Object value) {
		return Long.parseLong(String.valueOf(value));
	}

	private String expirationMember(UUID gameId, UUID userId) {
		return gameId + ":" + userId;
	}
}
