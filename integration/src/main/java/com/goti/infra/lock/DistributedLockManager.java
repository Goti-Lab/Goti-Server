package com.goti.infra.lock;

import com.goti.config.properties.DistributedLockProperties;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class DistributedLockManager {
	private final RedissonClient redissonClient;
	private final DistributedLockProperties distributedLockProperties;

	public <T> T withLock(String lockKey, Supplier<T> action) {
		RLock lock = redissonClient.getLock(lockKey);
		boolean acquired = false;

		try {
			acquired = lock.tryLock(distributedLockProperties.waitSeconds(), TimeUnit.SECONDS);
			Preconditions.validate(
				acquired,
				ErrorCode.SEAT_LOCK_ACQUIRE_FAILED
			);
			return action.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, e);
		} finally {
			if (acquired && lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}

	public boolean withLockIfAvailable(String lockKey, Runnable action) {
		RLock lock = redissonClient.getLock(lockKey);
		boolean acquired = false;

		try {
			acquired = lock.tryLock(distributedLockProperties.waitSeconds(), TimeUnit.SECONDS);
			if (!acquired) {
				return false;
			}
			action.run();
			return true;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR, e);
		} finally {
			if (acquired && lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}
}
