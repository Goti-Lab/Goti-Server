package com.goti.queue.scheduler;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.goti.queue.repository.QueueRedisRepository;
import com.goti.queue.service.QueueLeaveService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class QueueExpirationScheduler {

	private final QueueRedisRepository queueRedisRepository;
	private final QueueLeaveService queueLeaveService;

	@Scheduled(fixedDelayString = "${queue.expiration-check-interval:30000}")
	public void expireAdmittedUsers() {
		Set<Object> expiredUsers = queueRedisRepository.getExpiredUsers(Instant.now());
		if (expiredUsers == null || expiredUsers.isEmpty()) {
			return;
		}

		for (Object expiredUser : expiredUsers) {
			String member = String.valueOf(expiredUser);
			String[] parts = member.split(":");
			if (parts.length != 2) {
				continue;
			}

			UUID gameId = UUID.fromString(parts[0]);
			UUID userId = UUID.fromString(parts[1]);
			queueLeaveService.expire(gameId, userId);
		}
	}
}
