package com.goti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.goti.payment.GotiPaymentApplication;
import com.goti.resale.GotiResaleApplication;
import com.goti.stadium.GotiStadiumApplication;
import com.goti.ticketing.GotiTicketingApplication;
import com.goti.user.GotiUserApplication;

@ConfigurationPropertiesScan
@SpringBootApplication
@ComponentScan(
	basePackages = "com.goti",
	excludeFilters = @ComponentScan.Filter(
		type = FilterType.ASSIGNABLE_TYPE,
		classes = {
			GotiUserApplication.class,
			GotiStadiumApplication.class,
			GotiTicketingApplication.class,
			GotiPaymentApplication.class,
			GotiResaleApplication.class,
			GotiQueueApplication.class
		}
	)
)
@EnableScheduling
@EnableAsync
public class GotiApplication {

	public static void main(String[] args) {
		SpringApplication.run(GotiApplication.class, args);
	}

}
