package com.goti.infra.cache;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.goti.infra.constants.redis.RedisKey;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisCache {
	private final RedisTemplate<String, Object> redisTemplate;

	public <T> void set(String key, T value) {
		redisTemplate.opsForValue().set(key, value);
	}

	public <T> void set(String key, T value, Duration ttl) {
		redisTemplate.opsForValue().set(key, value, ttl.toMillis(), TimeUnit.MILLISECONDS);
	}

	public <T> void set(RedisKey redisKey, Object keyParam, T value) {
		set(redisKey.getKey(keyParam), value, redisKey.getTtl());
	}

	public <T> T get(String key, Class<T> clazz) {
		Object value = redisTemplate.opsForValue().get(key);
		return value != null ? clazz.cast(value) : null;
	}

	public boolean consume(String key) {
		Boolean deleted = redisTemplate.delete(key);
		return Boolean.TRUE.equals(deleted);
	}

	public boolean hasKey(String key) {
		return redisTemplate.hasKey(key);
	}
}