package com.goti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@ConfigurationPropertiesScan(basePackages = "com.goti")
@SpringBootApplication(scanBasePackages = {
	"com.goti.queue",
	"com.goti.config",
	"com.goti.infra",
	"com.goti.domain.base",
	"com.goti.exception",
	"com.goti.global"
})
public class GotiQueueApplication {

	public static void main(String[] args) {
		SpringApplication.run(GotiQueueApplication.class, args);
	}

}