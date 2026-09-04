package com.project.jredis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JredisApplication {
	public static void main(String[] args) {
		SpringApplication.run(JredisApplication.class, args);
	}


}