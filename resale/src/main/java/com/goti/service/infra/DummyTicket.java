package com.goti.service.infra;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.dto.internal.ResaleTicketResponse;
import com.goti.dto.internal.TicketGameInfo;
import com.goti.infra.TicketClient;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DummyTicket implements TicketClient {

	@Override
	public ResaleTicketResponse getTicketInfo(UUID ticketId, UUID ownerId) {
		// 테스트용 더미 데이터
		return new ResaleTicketResponse(
			ticketId,
			ownerId,
			UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939f"),
			UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939e"),
			UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939c"),
			UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939d"),
			"A구역 3열 15번",
			50000,
			LocalDateTime.now().plusDays(3),
			null,
			Instant.now()
		);
	}

	@Override
	public int getOwnedTicketCount(UUID userId, UUID gameId) {
		return 1;
	}

	@Override
	public List<UUID> getExpiredGameIds(LocalDateTime thresholdTime) {
		return List.of(UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939e"));
	}

	@Override
	public List<TicketGameInfo> getUpcomingGames() {
		return List.of(
			new TicketGameInfo(
				UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939f"),
				UUID.fromString("8df84c70-833e-4374-85ad-fa52f92f939d")
			)
		);
	}

	@Override
	public void transferOwnership(UUID ticketId, UUID buyerId) {
		// TODO: 실제 티켓 소유권 이전 로직 구현 (또는 다른 모듈로 이벤트 발행)
	}
}
