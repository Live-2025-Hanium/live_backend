package com.example.live_backend.infra.database;

import org.hibernate.boot.model.naming.PhysicalNamingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.example.live_backend.infra.strategy.UpperSnakeNamingStrategy;

@EnableJpaAuditing
@Configuration
public class JpaConfig {
	@Bean
	public PhysicalNamingStrategy physicalNamingStrategy() {
		return new UpperSnakeNamingStrategy();
	}
}
