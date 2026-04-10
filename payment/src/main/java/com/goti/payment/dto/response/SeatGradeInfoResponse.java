package com.goti.payment.dto.response;

import java.util.List;

public record SeatGradeInfoResponse(
	String seatGradeName,
	List<String> seatInfos
) {
}
