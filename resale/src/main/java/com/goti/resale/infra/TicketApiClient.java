package com.goti.resale.infra;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.config.properties.ApiEndpointProperties;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.resale.dto.response.ResaleTicketResponse;
import com.goti.resale.infra.dto.TicketGameInfo;
import com.goti.resale.infra.dto.TicketTransferRequest;

@Component
public class TicketApiClient extends BaseRestClient implements TicketClient {
	private static final String TICKETING_RESALE_API = "/api/v1/tickets/resales";
	private static final String PATH_SEPARATOR = "/";

	public TicketApiClient(RestClient.Builder builder, ApiEndpointProperties properties) {
		super(builder, properties.ticketing());
	}

	@Override
	public ResaleTicketResponse getTicketInfo(UUID ticketId, UUID userId) {
		String uri = TICKETING_RESALE_API + PATH_SEPARATOR + ticketId;
		return getGotiResponse(
			uri,
			null,
			Map.of("userId", userId),
			new ParameterizedTypeReference<>() {
			}
		);
	}

	@Override
	public int getOwnedTicketCount(UUID userId, UUID gameId) {
		String uri = TICKETING_RESALE_API + PATH_SEPARATOR + "count";
		return getGotiResponse(
			uri,
			null,
			Map.of("userId", userId, "gameId", gameId),
			new ParameterizedTypeReference<>() {
			}
		);
	}

	@Override
	public List<UUID> getExpiredGameIds(LocalDateTime thresholdTime) {
		String uri = TICKETING_RESALE_API + PATH_SEPARATOR + "expired";
		return getGotiResponse(
			uri,
			null,
			Map.of("threshold", thresholdTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
			new ParameterizedTypeReference<>() {
			}
		);
	}

	@Override
	public List<TicketGameInfo> getUpcomingGames() {
		String uri = TICKETING_RESALE_API + PATH_SEPARATOR + "upcoming";
		return getGotiResponse(
			uri,
			null,
			null,
			new ParameterizedTypeReference<>() {
			}
		);
	}

	@Override
	public void markAsResaleListing(UUID ticketId, UUID userId) {
		String uri = TICKETING_RESALE_API + PATH_SEPARATOR + ticketId + PATH_SEPARATOR + "listing";
		patchVoid(uri, Map.of("userId", userId));
	}

	@Override
	public void cancelResaleListing(UUID ticketId, UUID userId) {
		String uri = TICKETING_RESALE_API + PATH_SEPARATOR + ticketId + PATH_SEPARATOR + "cancel";
		patchVoid(uri, Map.of("userId", userId));
	}

	@Override
	public void transferOwnership(
		UUID ticketId,
		UUID buyerId,
		String buyerNickname,
		String buyerEmail,
		String buyerPhone,
		UUID transactionId,
		Integer transactionPrice
	) {
		String uri = TICKETING_RESALE_API + PATH_SEPARATOR + ticketId + PATH_SEPARATOR + "transfer";
		TicketTransferRequest request = new TicketTransferRequest(
			buyerId,
			buyerNickname,
			buyerEmail,
			buyerPhone,
			transactionId,
			transactionPrice
		);
		postVoid(uri, request);
	}
}
