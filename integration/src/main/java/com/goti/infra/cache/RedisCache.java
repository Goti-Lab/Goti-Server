package com.goti.infra.cache;

import com.fasterxml.jackson.databind.ObjectMapper;

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
	private final ObjectMapper objectMapper;

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
		if (value == null) {
			return null;
		}
		if (clazz.isInstance(value)) {
			return clazz.cast(value);
		}
		return objectMapper.convertValue(value, clazz);
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
