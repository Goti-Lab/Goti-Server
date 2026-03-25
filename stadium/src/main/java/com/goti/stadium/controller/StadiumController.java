package com.goti.stadium.controller;

import com.goti.stadium.domain.entity.stadium.StadiumEntity;
import com.goti.stadium.dto.request.StadiumCreateRequest;
import com.goti.stadium.dto.response.StadiumCreateResponse;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.stadium.dto.response.internal.StadiumLocationResponse;
import com.goti.stadium.service.domain.stadium.StadiumService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.goti.global.api.ApiSuccessResponse.wrap;

@Tag(name = "Stadium", description = "야구구장 관련 API")
@RestController
@RequestMapping("/api/v1/stadiums")
@RequiredArgsConstructor
public class StadiumController {
	
	private final StadiumService stadiumService;

	@Operation(
		summary = "야구 구장 생성",
		description = "야구 구장 생성 API"
	)
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<StadiumCreateResponse>> create(
		@RequestBody @Valid StadiumCreateRequest request
	) {
		return wrap(
			stadiumService.create(
				request.stadiumName(),
				request.location(),
				request.city(),
				request.district(),
				request.roadAddress(),
				request.latitude(),
				request.longitude(),
				request.totalSeats(),
				request.seatMapConfig()
			)
		);
	}

	@Operation(
		summary = "야구 구장 조회",
		description = "야구 구장 조회 API"
	)
	@GetMapping("/{stadiumId}")
	public ResponseEntity<ApiSuccessResponse<StadiumEntity>> get(
		@PathVariable UUID stadiumId
	) {
		return wrap(stadiumService.getById(stadiumId));
	}

	@GetMapping
	public ResponseEntity<ApiSuccessResponse<List<StadiumLocationResponse>>> getLocationsByIds(
		@RequestParam List<UUID> stadiumIds
	) {
		return wrap(
			stadiumService.getLocationsByIds(stadiumIds)
		);
	}
}
