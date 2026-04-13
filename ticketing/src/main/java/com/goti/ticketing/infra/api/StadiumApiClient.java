package com.goti.ticketing.infra.api;

import com.goti.config.properties.ApiEndpointProperties;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.infra.api.base.BaseRestClient;

import com.goti.ticketing.infra.api.dto.response.StadiumLocationResponse;
import com.goti.ticketing.infra.api.dto.response.BaseballTeamDisplayNameResponse;

import com.goti.ticketing.infra.api.dto.response.StadiumTotalSeatsResponse;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class StadiumApiClient extends BaseRestClient implements StadiumClient {

	private final static String BASEBALL_TEAM_GET_API = "/api/v1/baseball-teams";
	private final static String STADIUM_GET_API = "/api/v1/stadiums";
	private final static String STADIUM_INTERNAL_API = "/internal/stadiums";
	private final static String PATH_SEPARATOR = "/";

	public StadiumApiClient(RestClient.Builder builder, ApiEndpointProperties properties) {
		super(builder, properties.stadium());
	}

	@Override
	public void validateBaseballTeam(UUID teamId) {
		String uri = BASEBALL_TEAM_GET_API + PATH_SEPARATOR + teamId;
		getVoid(uri, null, null);
	}

	@Override
	public void validateStadium(UUID stadiumId) {
		String uri = STADIUM_GET_API + PATH_SEPARATOR + stadiumId;
		getVoid(uri, null, null);
	}

	@Override
	public List<BaseballTeamDisplayNameResponse> getBaseballTeamDisplayNames(List<UUID> teamIds) {
		Map<String, Object> queryParams = Map.of(
			"teamIds", teamIds.stream()
				.map(UUID::toString)
				.collect(Collectors.joining(","))
		);

		var response = getGotiResponse(
			BASEBALL_TEAM_GET_API,
			null,
			queryParams,
			new ParameterizedTypeReference<ApiSuccessResponse<List<BaseballTeamDisplayNameResponse>>>() {}
		);
		return response != null ? response : Collections.emptyList();
	}

	@Override
	public List<StadiumLocationResponse> getStadiumLocations(List<UUID> stadiumIds) {
		Map<String, Object> queryParams = Map.of(
			"stadiumIds", stadiumIds.stream()
				.map(UUID::toString)
				.collect(Collectors.joining(","))
		);
		var response = getGotiResponse(
			STADIUM_GET_API,
			null,
			queryParams,
			new ParameterizedTypeReference<ApiSuccessResponse<List<StadiumLocationResponse>>>() {}
		);
		return response != null ? response : Collections.emptyList();
	}

	@Override
	public StadiumTotalSeatsResponse getStadiumTotalSeats(UUID stadiumId) {
		String uri = STADIUM_INTERNAL_API + PATH_SEPARATOR + stadiumId + PATH_SEPARATOR + "total-seats";
		return get(uri, null, null, StadiumTotalSeatsResponse.class);
	}

}
