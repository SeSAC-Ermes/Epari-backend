package com.example.epari;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EpariBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(EpariBackendApplication.class, args);
	}

}
