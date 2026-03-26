package com.goti.infra.cache;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
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

	public Set<String> getKeys(String pattern) {
		return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
			Set<String> keys = new HashSet<>();
			ScanOptions options = ScanOptions.scanOptions()
				.match(pattern)
				.count(100)
				.build();

			try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
				while (cursor.hasNext()) {
					keys.add(new String(cursor.next()));
				}
			} catch (Exception e) {
				throw new RuntimeException("Redis SCAN 중 오류 발생", e);
			}
			return keys;
		});
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

	public void zAdd(String key, Object value, double score) {
		redisTemplate.opsForZSet().add(key, value, score);
	}

	public Long zRank(String key, Object value) {
		return redisTemplate.opsForZSet().rank(key, value);
	}

	public Long zSize(String key) {
		return redisTemplate.opsForZSet().zCard(key);
	}

	public Double zScore(String key, Object value) {
		return redisTemplate.opsForZSet().score(key, value);
	}

	public Set<Object> zRange(String key, long start, long end) {
		return redisTemplate.opsForZSet().range(key, start, end);
	}

	public void zRemove(String key, Object... values) {
		redisTemplate.opsForZSet().remove(key, values);
	}

	public long countKeys(String pattern) {
		Long count = redisTemplate.execute((RedisCallback<Long>) connection -> {
			long result = 0;
			ScanOptions options = ScanOptions.scanOptions()
				.match(pattern + "*")
				.count(100)
				.build();

			try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
				while (cursor.hasNext()) {
					cursor.next();
					result++;
				}
			} catch (Exception e) {
				throw new RuntimeException("Redis SCAN 중 오류 발생", e);
			}
			return result;
		});
		return count != null ? count : 0L;
	}
}