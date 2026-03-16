package com.goti.stadium.dto.response;

import com.goti.stadium.constants.StadiumType;

import com.goti.stadium.constants.TeamCode;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "홈구장 생성 응답")
public record HomeStadiumCreateResponse(

	@Schema(description = "홈구장 ID", example = "c2123239-12bb-bded-6113-01cq2bae3ex4")
	UUID homeStadiumId,

	@Schema(description = "구단 ID", example = "c2123239-12bb-bded-6113-01cq2bae3ex4")
	UUID baseballTeamId,

	@Schema(description = "구장 ID", example = "c2123239-12bb-bded-6113-01cq2bae3ex4")
	UUID stadiumId,

	@Schema(description = "팀코드", example = "SS")
	TeamCode teamCode,

	@Schema(description = "구단명", example = "삼성 라이온즈")
	String teamName,

	@Schema(description = "구단명(영문)", example = "Samsung Lions")
	String teamNameEn,

	@Schema(description = "구장명", example = "대구삼성라이온즈파크")
	String stadiumName,

	@Schema(description = "지역명", example = "대구")
	String location,

	@Schema(description = "구장 타입", example = "제1구장")
	StadiumType type
) {
	public static HomeStadiumCreateResponse from(
		UUID homeStadiumId,
		UUID baseballTeamId,
		UUID stadiumId,
		TeamCode teamCode,
		String teamName,
		String teamNameEn,
		String stadiumName,
		String location,
		StadiumType type
	) {
		return new HomeStadiumCreateResponse(
			homeStadiumId, baseballTeamId, stadiumId, teamCode,
			teamName, teamNameEn, stadiumName, location, type
		);
	}
}
