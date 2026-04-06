package com.goti.resale.infra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.goti.resale.dto.response.ResaleTicketResponse;
import com.goti.resale.infra.dto.ResaleTicketPurchaseInfo;
import com.goti.resale.infra.dto.GameScheduleResponse;
import com.goti.resale.infra.dto.TicketGameInfo;

public interface TicketClient {
	ResaleTicketResponse getTicketInfo(UUID ticketId, UUID ownerId);

	int getOwnedTicketCount(UUID userId, UUID gameId);

	List<UUID> getExpiredGameIds(LocalDateTime thresholdTime);

	List<TicketGameInfo> getUpcomingGames();

	GameScheduleResponse getGameSchedule(UUID gameId);

	void markAsResaleListing(UUID ticketId, UUID userId);

	void cancelResaleListing(UUID ticketId, UUID userId);

	List<ResaleTicketPurchaseInfo> getPurchaseInfos(List<UUID> ticketIds);

	void transferOwnership(
		UUID ticketId,
		UUID buyerId,
		String buyerNickname,
		String buyerEmail,
		String buyerPhone,
		UUID transactionId,
		Integer transactionPrice,
		String authToken
	);
}
