package com.goti.ticket.service.application;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.f4b6a3.tsid.TsidCreator;
import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.ticket.TicketEntity;
import com.goti.exception.CustomException;
import com.goti.global.validation.Preconditions;
import com.goti.infra.cache.RedisCache;
import com.goti.infra.constants.redis.RedisKey;
import com.goti.ticket.dto.response.TicketQrResponse;
import com.goti.ticket.repository.TicketRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketQrService {
	private static final String QR_PREFIX = "QR";

	private final TicketRepository ticketRepository;
	private final RedisCache redisCache;

	@Transactional(readOnly = true)
	public TicketQrResponse create(
		UUID ticketId,
		UUID memberId
	) {
		Preconditions.validate(
			memberId != null,
			ErrorCode.AUTH_INVALID
		);

		TicketEntity ticket = ticketRepository.findByIdAndUserId(ticketId, memberId)
			.orElseThrow(() -> new CustomException(ErrorCode.TICKET_NOT_FOUND));

		return createQrToken(ticket);
	}

	private TicketQrResponse createQrToken(TicketEntity ticket) {
		String qrToken = generateQrToken();
		LocalDateTime expiresAt = LocalDateTime.now().plus(RedisKey.TICKET_QR.getTtl());

		redisCache.set(
			RedisKey.TICKET_QR,
			ticket.getId(),
			qrToken
		);

		return TicketQrResponse.from(ticket.getId(), qrToken, expiresAt);
	}

	private String generateQrToken() {
		return QR_PREFIX + TsidCreator.getTsid().toString();
	}
}
