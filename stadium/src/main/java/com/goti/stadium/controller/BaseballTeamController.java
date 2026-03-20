package com.goti.stadium.controller;

import static com.goti.global.api.ApiSuccessResponse.*;

import java.util.UUID;

import com.goti.stadium.domain.entity.team.BaseballTeamEntity;

import io.swagger.v3.oas.annotations.Operation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.goti.stadium.dto.request.BaseballTeamCreateRequest;
import com.goti.stadium.dto.request.HomeStadiumCreateRequest;
import com.goti.stadium.dto.response.BaseballTeamCreateResponse;
import com.goti.stadium.dto.response.HomeStadiumCreateResponse;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.stadium.service.application.HomeStadiumManagementService;
import com.goti.stadium.service.domain.baseballteam.BaseballTeamService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "BaseballTeam", description = "야구구단(팀) 관련 API")
@RestController
@RequestMapping("/api/v1/baseball-teams")
@RequiredArgsConstructor
public class BaseballTeamController {

	private final BaseballTeamService baseballTeamService;
	private final HomeStadiumManagementService homeStadiumManagementService;

	@Operation(
		summary = "구단(팀) 생성",
		description = "야구구단(팀) 생성 API"
	)
	@PostMapping
	public ResponseEntity<ApiSuccessResponse<BaseballTeamCreateResponse>> register(
		@RequestBody @Valid BaseballTeamCreateRequest request
	) {
		return wrap(
			baseballTeamService.create(request.toCommand())
		);
	}

	@Operation(
		summary = "구단(팀) 상세 조회",
		description = "야구구단(팀) 상세 조회 API"
	)
	@GetMapping("/{teamId}")
	public ResponseEntity<ApiSuccessResponse<BaseballTeamEntity>> get(
		@PathVariable UUID teamId
	) {
		return wrap(
			baseballTeamService.getById(teamId)
		);
	}

	@Operation(
		summary = "구단(팀) 홈구장 등록",
		description = "야구구단(팀) 홈구장(제1구장, 제2구장) 등록 API"
	)
	@PostMapping("/{teamId}/home-stadiums")
	public ResponseEntity<ApiSuccessResponse<HomeStadiumCreateResponse>> assignHomeStadium(
		@PathVariable UUID teamId,
		@RequestBody @Valid HomeStadiumCreateRequest request
	) {
		return wrap(
			homeStadiumManagementService.assignHomeStadium(
				teamId, request.stadiumId(), request.type()
			)
		);
	}

}
