package com.goti.infra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.goti.dto.internal.ResaleTicketResponse;
import com.goti.dto.internal.TicketGameInfo;

/**
 * 티켓 도메인과의 연동을 위한 인터페이스
 */
public interface TicketClient {
	ResaleTicketResponse getTicketInfo(UUID ticketId, UUID ownerId);

	int getOwnedTicketCount(UUID userId, UUID gameId);

	List<UUID> getExpiredGameIds(LocalDateTime thresholdTime);

	List<TicketGameInfo> getUpcomingGames();

	void transferOwnership(UUID ticketId, UUID buyerId);
}
