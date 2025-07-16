package com.example.live_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LiveBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(LiveBackendApplication.class, args);
		System.out.println("Live Backend is running!");
	}
}
