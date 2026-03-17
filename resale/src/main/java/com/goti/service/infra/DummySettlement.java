package com.goti.service.infra;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.goti.resale.infra.SettlementClient;

@Service
public class DummySettlement implements SettlementClient {

	@Override
	public void processSettlement(UUID resaleOrderId) {
		// TODO: 정산 서비스의 API를 호출하거나 정산 이벤트를 발행합니다.
	}
}
