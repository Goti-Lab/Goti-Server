package com.goti.order.service.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.game.GameScheduleEntity;
import com.goti.domain.entity.order.OrderEntity;
import com.goti.exception.CustomException;
import com.goti.game.repository.GameScheduleRepository;
import com.goti.order.dto.request.OrderCreateRequest;
import com.goti.order.dto.response.OrderCreateResponse;
import com.goti.order.service.domain.OrderHistoryService;
import com.goti.order.service.domain.OrderService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderCreateService {
	private final GameScheduleRepository gameScheduleRepository;
	private final OrderService orderService;
	private final OrderHistoryService orderHistoryService;

	@Transactional
	public OrderCreateResponse create(
		UUID userId,
		OrderCreateRequest request
	) {
		GameScheduleEntity gameSchedule = gameScheduleRepository.findById(request.gameId())
			.orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

		OrderEntity order = orderService.create(
			userId,
			gameSchedule,
			request.holdIds().size(),
			1
		);

		orderHistoryService.create(
			order,
			request.ordererName(),
			request.ordererPhone(),
			request.ordererEmail()
		);

		return OrderCreateResponse.from(
			order.getId(),
			order.getOrderNumber(),
			order.getGameSchedule().getId(),
			order.getOrderStatus(),
			order.getTotalQuantity(),
			order.getTotalAmount()
		);
	}
}
