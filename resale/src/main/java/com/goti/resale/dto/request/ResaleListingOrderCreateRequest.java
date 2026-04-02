package com.goti.resale.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record ResaleListingOrderCreateRequest(
	@Schema(description = "리셀 등록 목록")
	@NotEmpty(message = "리셀 등록 정보는 필수입니다.")
	@Valid
	List<ResaleListingCreateRequest> listings
) {
}
