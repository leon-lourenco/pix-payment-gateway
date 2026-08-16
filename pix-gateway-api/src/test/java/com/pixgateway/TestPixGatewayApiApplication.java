package com.pixgateway;

import org.springframework.boot.SpringApplication;

public class TestPixGatewayApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(PixGatewayApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
