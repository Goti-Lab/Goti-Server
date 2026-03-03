package com.goti.service;

import java.util.UUID;

import com.goti.dto.response.ResaleGameResponse;

/**
 * Game 도메인과의 통신 인터페이스
 * MSA 전환 시 RestClient로 대체
 */
public interface GameService {

	ResaleGameResponse getGameForResale(UUID gameId);
}