package com.goti.ticket.service.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.domain.entity.order.OrderEntity;
import com.goti.domain.entity.order.OrderItemEntity;
import com.goti.domain.entity.order.OrderHistoryEntity;
import com.goti.exception.CustomException;
import com.goti.order.repository.OrderItemRepository;
import com.goti.order.repository.OrderHistoryRepository;
import com.goti.ticket.dto.response.TicketResponse;
import com.goti.ticket.service.domain.TicketService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketCreateService {
	private final OrderHistoryRepository orderHistoryRepository;
	private final OrderItemRepository orderItemRepository;
	private final TicketService ticketService;

	@Transactional
	public List<TicketResponse> create(OrderEntity order) {
		OrderHistoryEntity orderHistory = orderHistoryRepository.findByOrder_Id(order.getId())
			.orElseThrow(() -> new CustomException(ErrorCode.ORDER_HISTORY_NOT_FOUND));

		//TODO: 경기 제목 추가
		return orderItemRepository.findOrderItemsByOrderId(order.getId()).stream()
			.map(orderItem -> ticketService.create(
				orderItem,
				order.getGameSchedule().getId(),
				order.getUserId(),
				orderHistory.getName(),
				orderHistory.getEmail(),
				orderHistory.getMobile(),
				null,
				order.getGameSchedule().getStartAt(),
				buildSeatInfo(orderItem),
				orderItem.getTicketPrice()
			))
			.map(TicketResponse::from)
			.toList();
	}

	private String buildSeatInfo(OrderItemEntity orderItem) {
		return orderItem.getSeat().getSeatSection().getSectionCode() +
			" " +
			orderItem.getSeat().getRowName() +
			"-" +
			orderItem.getSeat().getSeatNum();
	}
}
