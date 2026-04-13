package com.goti.ticketing.infra.api;

import com.goti.ticketing.infra.api.dto.response.StadiumLocationResponse;
import com.goti.ticketing.infra.api.dto.response.BaseballTeamDisplayNameResponse;
import com.goti.ticketing.infra.api.dto.response.StadiumTotalSeatsResponse;

import java.util.List;
import java.util.UUID;

public interface StadiumClient {
	void validateBaseballTeam(UUID teamId);
	void validateStadium(UUID stadiumId);
	List<BaseballTeamDisplayNameResponse> getBaseballTeamDisplayNames(List<UUID> teamIds);
	List<StadiumLocationResponse> getStadiumLocations(List<UUID> stadiumIds);
	StadiumTotalSeatsResponse getStadiumTotalSeats(UUID stadiumId);
}
