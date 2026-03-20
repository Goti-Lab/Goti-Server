package com.goti.ticketing.infra.api;

import java.util.UUID;

public interface StadiumClient {
	void validateBaseballTeam(UUID teamId);
	void validateStadium(UUID stadiumId);
}
