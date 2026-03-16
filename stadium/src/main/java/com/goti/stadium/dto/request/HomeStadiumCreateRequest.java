package com.goti.stadium.dto.request;

import com.goti.stadium.constants.StadiumType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "홈구장 생성")
public record HomeStadiumCreateRequest(

	@Schema(description = "구장 ID", example = "fe512439-b6bb-4aed-8612-02f34bae3ec5")
	@NotNull(message = "구장 ID는 필수 항목입니다.")
	UUID stadiumId,

	@Schema(description = "구장 타입", example = "PRIMARY")
	@NotNull(message = "구장 타입은 필수 항목입니다.")
	StadiumType type
) {
}
