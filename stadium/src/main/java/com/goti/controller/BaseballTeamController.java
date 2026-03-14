package com.goti.controller;

import com.goti.dto.request.BaseballTeamCreateRequest;
import com.goti.dto.response.BaseballTeamCreateResponse;
import com.goti.global.api.ApiSuccessResponse;
import com.goti.service.domain.baseballteam.BaseballTeamService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.goti.global.api.ApiSuccessResponse.wrap;

@Tag(name = "BaseballTeam", description = "야구구단(팀) 관련 API")
@RestController
@RequestMapping("/api/v1/baseball-teams")
@RequiredArgsConstructor
public class BaseballTeamController {

	private final BaseballTeamService baseballTeamService;

	@PostMapping
	public ResponseEntity<ApiSuccessResponse<BaseballTeamCreateResponse>> register(
		@RequestBody @Valid BaseballTeamCreateRequest request
	) {
		return wrap(
			baseballTeamService.create(request.toCommand())
		);
	}

}
