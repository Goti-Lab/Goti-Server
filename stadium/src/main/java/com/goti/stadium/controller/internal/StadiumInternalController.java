package com.goti.stadium.controller.internal;

import com.goti.stadium.dto.response.internal.StadiumTotalSeatsResponse;

import com.goti.stadium.service.internal.StadiumInternalService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/stadiums")
@RequiredArgsConstructor
public class StadiumInternalController {

	private final StadiumInternalService stadiumInternalService;

	@GetMapping("/{stadiumId}")
	public StadiumTotalSeatsResponse getTotalSeats(
		@PathVariable UUID stadiumId
	) {
		return stadiumInternalService.getTotalSeats(stadiumId);
	}
}
