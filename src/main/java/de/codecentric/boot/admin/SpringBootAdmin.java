package de.codecentric.boot.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import de.codecentric.boot.admin.config.WebappConfig;

@Configuration
@EnableAutoConfiguration
@Import(WebappConfig.class)
public class SpringBootAdmin {

	/**
	 * Starting point for application to boot.
	 * 
	 * @param args
	 *            Passed arguments.
	 */
	public static void main(String[] args) {
		SpringApplication.run(SpringBootAdmin.class, args);
	}

}
