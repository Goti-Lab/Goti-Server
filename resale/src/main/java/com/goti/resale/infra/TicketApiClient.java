package com.goti.resale.infra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.goti.config.properties.ApiEndpointProperties;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.resale.dto.response.ResaleTicketResponse;
import com.goti.resale.infra.dto.ResaleTicketPurchaseInfo;
import com.goti.resale.infra.dto.TicketGameInfo;

@Component
public class TicketApiClient extends BaseRestClient implements TicketClient {
	private static final String TICKET_API = "/api/v1/tickets";

	public TicketApiClient(RestClient.Builder builder, ApiEndpointProperties properties) {
		super(builder, properties.ticketing());
	}

	@Override
	public List<ResaleTicketPurchaseInfo> getPurchaseInfos(List<UUID> ticketIds) {
		String uri = String.format("%s/purchase-infos", TICKET_API);
		Map<String, String> queryParams = Map.of(
			"ticketIds",
			ticketIds.stream()
				.map(UUID::toString)
				.collect(Collectors.joining(","))
		);

		var response = getGotiResponse(
			uri,
			null,
			queryParams,
			new ParameterizedTypeReference<ApiSuccessResponse<List<ResaleTicketPurchaseInfo>>>() {}
		);

		if (response == null) {
			throw new CustomException(ErrorCode.INTERNAL_API_INVALID_RESPONSE);
		}

		return response;
	}

	@Override
	public ResaleTicketResponse getTicketInfo(UUID ticketId, UUID ownerId) {
		return null;
	}

	@Override
	public int getOwnedTicketCount(UUID userId, UUID gameId) {
		return 0;
	}

	@Override
	public List<UUID> getExpiredGameIds(LocalDateTime thresholdTime) {
		return null;
	}

	@Override
	public List<TicketGameInfo> getUpcomingGames() {
		return null;
	}

	@Override
	public void transferOwnership(UUID ticketId, UUID buyerId) {}
}
