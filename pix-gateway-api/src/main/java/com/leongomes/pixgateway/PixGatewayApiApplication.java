package com.leongomes.pixgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PixGatewayApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(PixGatewayApiApplication.class, args);
	}

}
