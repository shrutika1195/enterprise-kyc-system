package com.internship.kyc_system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication//Starting point of the application.Auto-Configuration
public class KycSystemApplication {
	public static void main(String[] args) {
		SpringApplication.run(KycSystemApplication.class, args);//boots up the embedded Tomcat server
	}
}