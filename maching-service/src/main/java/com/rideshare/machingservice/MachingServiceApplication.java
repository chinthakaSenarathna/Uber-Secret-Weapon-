package com.rideshare.machingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MachingServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(MachingServiceApplication.class, args);
	}

}
