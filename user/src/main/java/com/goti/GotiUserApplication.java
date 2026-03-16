package com.goti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class GotiUserApplication {

	public static void main(String[] args) {
		SpringApplication.run(GotiUserApplication.class, args);
	}

}
