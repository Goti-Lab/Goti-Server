package com.goti.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.dto.response.GameInfo;
import com.goti.dto.response.ResaleTicketResponse;

import lombok.RequiredArgsConstructor;

/**
 * Ticket 도메인과의 통신 인터페이스 추후 RestClient 등으로 변경
 */
@Service
@RequiredArgsConstructor
public class TicketService {
	public ResaleTicketResponse getTicketInfo(UUID ticketId, UUID ownerId) {
		// TODO: 실제 구현 시 Ticket 도메인과 통신

		// 테스트용 더미 데이터
		return new ResaleTicketResponse(
			ticketId,
			UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939f"),
			// 경기ID 고정 (경기당 횟수 테스트 5,3)
			// UUID.randomUUID(), // 경기아이디 랜덤 (10회 테스트)
			ownerId,
			UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939e"),
			UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939d"),
			"A구역 3열 15번",
			50000,
			LocalDateTime.now().plusDays(3)
		);
	}

	public List<UUID> getExpiredGameIds(LocalDateTime thresholdTime) {
		// TODO: 실제 구현 시 Ticket 도메인에서 쿼리를 이용하여 thresholdTime 이후의 게임 ID 목록을 반환
		// 임시 더미 데이터 (테스트용 게임 ID)
		return List.of(UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939e"));
	}

	public List<GameInfo> getUpcomingGames() {
		return List.of(
			GameInfo.of(
				UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939f"),
				UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939d")
			),
			GameInfo.of(
				UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939f"),
				UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939c")
			)
		);
	}
}