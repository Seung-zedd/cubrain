package com.cubrain.springboot_starter_auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class CubrainApplication {

	public static void main(String[] args) {
		SpringApplication.run(CubrainApplication.class, args);
	}

}
