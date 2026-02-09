package com.introms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class ResourceProcessorApp {

	public static void main(String[] args) {
		SpringApplication.run(ResourceProcessorApp.class, args);
	}

}
