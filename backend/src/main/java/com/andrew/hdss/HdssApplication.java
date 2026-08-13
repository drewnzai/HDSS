package com.andrew.hdss;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.andrew.hdss")
public class HdssApplication {

	public static void main(String[] args) {
		SpringApplication.run(HdssApplication.class, args);
	}

}
