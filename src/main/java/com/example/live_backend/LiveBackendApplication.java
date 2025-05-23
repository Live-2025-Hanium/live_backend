package com.example.live_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LiveBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(LiveBackendApplication.class, args);
		System.out.println("Live Backend is running!");
	}

}
