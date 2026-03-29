package com.goti.infra.cache;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goti.infra.constants.redis.RedisKey;

import lombok.RequiredArgsConstructor;

/**
 * Redis 캐시 유틸리티.
 * TODO: RedisTemplate&lt;String, String&gt; + 도메인별 Repository 패턴으로 전환 예정
 *       (docs/conventions/redis-serialization-guide.md 참조)
 */
@Component
@RequiredArgsConstructor
public class RedisCache {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper redisObjectMapper;

	public <T> void set(String key, T value) {
		redisTemplate.opsForValue().set(key, value);
	}

	public <T> void set(String key, T value, Duration ttl) {
		redisTemplate.opsForValue().set(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
	}

	public <T> void set(RedisKey redisKey, Object keyParam, T value) {
		set(redisKey.getKey(keyParam), value, redisKey.getTtl());
	}

	public <T> void set(RedisKey redisKey, T value, Object[] keyParams, Duration ttl) {
		set(redisKey.getKey(keyParams), value, ttl);
	}

	public <T> T get(String key, Class<T> clazz) {
		Object value = redisTemplate.opsForValue().get(key);
		if (value == null) {
			return null;
		}
		if (clazz.isInstance(value)) {
			return clazz.cast(value);
		}
		return redisObjectMapper.convertValue(value, clazz);
	}

	public boolean delete(String key) {
		Boolean result = redisTemplate.delete(key);
		return Boolean.TRUE.equals(result);
	}

	public boolean consume(String key) {
		return delete(key);
	}

	public boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}
}
