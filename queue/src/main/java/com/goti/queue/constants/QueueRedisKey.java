package com.goti.queue.constants;

import java.time.Duration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueueRedisKey {
	/**
	 * 대기열 순번 발급용 sequence.
	 * key: queue:{gameId}:sequence
	 */
	SEQUENCE("queue:%s:sequence", null),

	/**
	 * 대기 중인 유저 정렬 집합.
	 * key: queue:{gameId}:waiting
	 * member: userId, score: queueNumber
	 */
	WAITING("queue:%s:waiting", null),

	/**
	 * 대기열 메타 정보 해시.
	 * key: queue:{gameId}:meta
	 */
	META("queue:%s:meta", null),

	/**
	 * 유저별 대기열 엔트리 저장.
	 * key: queue:{gameId}:entry:{userId}
	 */
	ENTRY("queue:%s:entry:%s", Duration.ofMinutes(30)),

	/**
	 * 활성 입장 유저 집합.
	 * key: queue:{gameId}:active-users
	 */
	ACTIVE_USERS("queue:%s:active-users", null);

	private final String pattern;
	private final Duration ttl;

	public String getKey(Object... args) {
		return pattern.formatted(args);
	}
}
