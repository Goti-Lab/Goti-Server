package com.goti.payment.infra;

import java.util.UUID;

/**
 * 에스크로 서버와 연결을 위한 클라이언트
 * 실제 에스크로 서비스는 사업자 등록 번호를 이용하여
 * 라이브 서비스에서만 테스트가 가능
 * Mock으로 구현
 */

public interface ResaleEscrowClient {
	String requestEscrowPayment(UUID transactionId, Integer amount);

	void requestSettlement(String escrowId);
}
