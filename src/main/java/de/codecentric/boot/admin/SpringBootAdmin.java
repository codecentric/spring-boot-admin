package de.codecentric.boot.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class SpringBootAdmin {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdmin.class, args);
	}

}
