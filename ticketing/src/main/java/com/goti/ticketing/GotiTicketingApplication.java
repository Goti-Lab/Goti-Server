package com.goti.ticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan(basePackages = "com.goti")
@SpringBootApplication(scanBasePackages = {
	"com.goti.ticketing",
	"com.goti.stadium", // TODO: MSA 분리 시 REST client/이벤트로 전환 후 제거
	"com.goti.config",
	"com.goti.infra",
	"com.goti.domain.base",
	"com.goti.exception",
	"com.goti.global"
})
@EnableScheduling
public class GotiTicketingApplication {

	public static void main(String[] args) {
		SpringApplication.run(GotiTicketingApplication.class, args);
	}

}
