package com.goti.ticketing.ticket.service.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.goti.constants.messages.ErrorCode;
import com.goti.ticketing.domain.entity.order.OrderEntity;
import com.goti.ticketing.domain.entity.order.OrderItemEntity;
import com.goti.ticketing.domain.entity.order.OrderHistoryEntity;
import com.goti.exception.CustomException;
import com.goti.ticketing.order.repository.OrderItemRepository;
import com.goti.ticketing.order.repository.OrderHistoryRepository;
import com.goti.ticketing.ticket.dto.response.TicketResponse;
import com.goti.ticketing.ticket.service.domain.TicketService;

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
				order.getMemberId(),
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
