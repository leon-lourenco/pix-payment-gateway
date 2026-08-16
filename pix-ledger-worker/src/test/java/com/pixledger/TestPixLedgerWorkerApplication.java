package com.pixledger;

import org.springframework.boot.SpringApplication;

public class TestPixLedgerWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.from(PixLedgerWorkerApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
