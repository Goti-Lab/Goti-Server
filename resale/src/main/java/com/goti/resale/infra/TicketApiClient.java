package com.goti.resale.infra;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.goti.config.properties.ApiEndpointProperties;
import com.goti.constants.messages.ErrorCode;
import com.goti.exception.CustomException;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.infra.api.base.BaseRestClient;
import com.goti.resale.dto.response.ResaleTicketResponse;
import com.goti.resale.infra.dto.ResaleTicketPurchaseInfo;
import com.goti.resale.infra.dto.GameScheduleResponse;
import com.goti.resale.infra.dto.TicketGameInfo;
import com.goti.resale.infra.dto.TicketTransferRequest;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class TicketApiClient extends BaseRestClient implements TicketClient {
	private static final String TICKET_API = "/api/v1/tickets";
	private static final String TICKETING_RESALE_API = "/api/v1/tickets/resales";
	private static final String TICKETING_GAME_API = "/api/v1/games";
	private static final String PATH_SEPARATOR = "/";

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
			getHeaders(),
			queryParams,
			new ParameterizedTypeReference<ApiSuccessResponse<List<ResaleTicketPurchaseInfo>>>() {
			}
		);

		if (response == null) {
			throw new CustomException(ErrorCode.INTERNAL_API_INVALID_RESPONSE);
		}

		return response;
	}

	@Override
	public ResaleTicketResponse getTicketInfo(UUID ticketId, UUID userId) {
		String uri = TICKETING_RESALE_API + PATH_SEPARATOR + ticketId;
		return getGotiResponse(
			uri,
			getHeaders(),
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
			getHeaders(),
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
			getHeaders(),
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
			getHeaders(),
			null,
			new ParameterizedTypeReference<>() {
			}
		);
	}

	@Override
	public void markAsResaleListing(UUID ticketId, UUID userId) {
		String uri = TICKETING_RESALE_API + PATH_SEPARATOR + ticketId + PATH_SEPARATOR + "listing";
		patchVoid(uri, getHeaders(), Map.of("userId", userId));
	}

	@Override
	public void cancelResaleListing(UUID ticketId, UUID userId) {
		String uri = TICKETING_RESALE_API + PATH_SEPARATOR + ticketId + PATH_SEPARATOR + "cancel";
		patchVoid(uri, getHeaders(), Map.of("userId", userId));
	}

	@Override
	public void transferOwnership(
		UUID ticketId,
		UUID buyerId,
		String buyerNickname,
		String buyerEmail,
		String buyerPhone,
		UUID transactionId,
		Integer transactionPrice,
		String authToken
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
		Map<String, String> headers = (authToken != null)
			? createBearerHeader(authToken)
			: getHeaders();

		postVoid(uri, headers, request);
	}

	private Map<String, String> getHeaders() {
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		if (attributes != null) {
			HttpServletRequest request = attributes.getRequest();
			String bearerToken = request.getHeader("Authorization");

			if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
				return createBearerHeader(bearerToken.substring(7)); // BaseRestClient의 헬퍼 메서드 활용
			}
		}
		return null;
	}

	@Override
	public GameScheduleResponse getGameSchedule(UUID gameId) {
		String uri = TICKETING_GAME_API + PATH_SEPARATOR + gameId + PATH_SEPARATOR + "schedule";
		return getGotiResponse(
			uri,
			null,
			null,
			new ParameterizedTypeReference<>() {
			}
		);
	}
}