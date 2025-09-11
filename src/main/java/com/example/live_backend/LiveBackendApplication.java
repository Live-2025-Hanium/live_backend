package com.example.live_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
@EnableCaching
public class LiveBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(LiveBackendApplication.class, args);
		System.out.println("Live Backend is running!");
	}
}
