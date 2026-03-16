package com.goti.infra;

import java.util.UUID;

/**
 * 정산 시스템과의 연동을 위한 인터페이스
 */
public interface SettlementClient {
	void processSettlement(UUID resaleOrderId);
}
