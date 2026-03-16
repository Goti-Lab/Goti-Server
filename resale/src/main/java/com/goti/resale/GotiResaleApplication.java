package com.goti.resale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConfigurationPropertiesScan(basePackages = "com.goti")
@SpringBootApplication(scanBasePackages = {
	"com.goti.resale",
	"com.goti.config",
	"com.goti.infra",
	"com.goti.domain.base",
	"com.goti.exception",
	"com.goti.global"
})
@EnableScheduling
public class GotiResaleApplication {

	public static void main(String[] args) {
		SpringApplication.run(GotiResaleApplication.class, args);
	}

}
