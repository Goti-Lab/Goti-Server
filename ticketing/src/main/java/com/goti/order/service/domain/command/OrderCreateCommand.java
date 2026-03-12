package com.goti.order.service.domain.command;

import java.util.List;
import java.util.UUID;

public record OrderCreateCommand(
	UUID gameId,
	UUID memberId,
	List<UUID> holdIds,
	String ordererName,
	String ordererPhone,
	String ordererEmail
) {
}
