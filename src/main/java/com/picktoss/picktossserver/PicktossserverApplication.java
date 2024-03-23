package com.picktoss.picktossserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class
PicktossserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(PicktossserverApplication.class, args);
	}

}
