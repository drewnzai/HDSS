package com.andrew.hdss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ComponentScan(basePackages = "com.andrew.hdss")
@EnableAsync
public class HdssApplication {

	public static void main(String[] args) {
		SpringApplication.run(HdssApplication.class, args);
	}

}
