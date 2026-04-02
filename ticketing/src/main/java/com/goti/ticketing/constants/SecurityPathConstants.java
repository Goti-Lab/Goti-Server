package com.goti.ticketing.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SecurityPathConstants {

	public static final String[] PUBLIC_URLS = {
		"/api/v1/games/**",
		"/api/v1/orders/*/payment-order",        // 내부용: payment → ticketing (mTLS로 보호)
		"/api/v1/orders/*/payment-confirmations", // 내부용: payment → ticketing (결제 확인 콜백)
		"/api/v1/tickets/purchase-infos",       // 내부용: resale → ticketing
	};
}
